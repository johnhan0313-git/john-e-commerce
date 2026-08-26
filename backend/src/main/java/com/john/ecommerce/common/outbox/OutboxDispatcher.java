package com.john.ecommerce.common.outbox;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.outbox.entity.EventOutbox;
import com.john.ecommerce.common.outbox.mapper.EventOutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * Polls PENDING (and retry-due) outbox rows and dispatches to {@link OutboxEventHandler} beans.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxDispatcher {

    private final EventOutboxMapper eventOutboxMapper;
    private final InboxService inboxService;
    private final List<OutboxEventHandler> handlers;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    @Value("${app.outbox.batch-size:50}")
    private int batchSize;

    @Value("${app.outbox.max-attempts:8}")
    private int maxAttempts;

    @Scheduled(fixedDelayString = "${app.outbox.poll-delay-ms:5000}")
    public void poll() {
        long now = System.currentTimeMillis();
        List<Long> tenantIds = jdbcTemplate.queryForList("""
                SELECT DISTINCT tenant_id FROM t_event_outbox
                 WHERE delete_flag = 0
                   AND (
                        (status = ? AND (next_retry_at IS NULL OR next_retry_at <= ?))
                     OR (status = ? AND updated_at < ?)
                   )
                """, Long.class,
                OutboxStatuses.PENDING, now,
                OutboxStatuses.PROCESSING, now - 300_000L);
        if (tenantIds.isEmpty()) return;

        int processed = 0;
        for (Long tenantId : tenantIds) {
            if (tenantId == null) continue;
            TenantContext.setTenantId(tenantId);
            try {
                processed += dispatchForCurrentTenant(now);
            } finally {
                TenantContext.clear();
            }
        }
        if (processed > 0) {
            log.info("OutboxDispatcher processed {} event(s) across {} tenant(s)", processed, tenantIds.size());
        }
    }

    private int dispatchForCurrentTenant(long now) {
        List<EventOutbox> rows = eventOutboxMapper.selectList(new LambdaQueryWrapper<EventOutbox>()
                .and(w -> w
                        .nested(n -> n.eq(EventOutbox::getStatus, OutboxStatuses.PENDING)
                                .and(a -> a.isNull(EventOutbox::getNextRetryAt)
                                        .or().le(EventOutbox::getNextRetryAt, now)))
                        .or(n -> n.eq(EventOutbox::getStatus, OutboxStatuses.PROCESSING)
                                .lt(EventOutbox::getUpdatedAt, now - 300_000L)))
                .orderByAsc(EventOutbox::getCreatedAt)
                .last("LIMIT " + Math.max(1, batchSize)));
        int count = 0;
        for (EventOutbox row : rows) {
            Long id = row.getId();
            try {
                Boolean ok = transactionTemplate.execute(status -> {
                    processOne(id);
                    return Boolean.TRUE;
                });
                if (Boolean.TRUE.equals(ok)) count++;
            } catch (Exception e) {
                log.warn("Outbox dispatch failed id={} type={}: {}", id, row.getEventType(), e.getMessage());
                transactionTemplate.executeWithoutResult(status -> markFailure(id, e));
            }
        }
        return count;
    }

    private void processOne(Long outboxId) {
        EventOutbox row = eventOutboxMapper.selectById(outboxId);
        if (row == null) return;
        if (row.getStatus() != null
                && row.getStatus() != OutboxStatuses.PENDING
                && row.getStatus() != OutboxStatuses.PROCESSING) {
            return;
        }
        long now = System.currentTimeMillis();
        if (row.getStatus() != null && row.getStatus() == OutboxStatuses.PENDING
                && row.getNextRetryAt() != null && row.getNextRetryAt() > now) {
            return;
        }

        int claimed = eventOutboxMapper.update(null, new LambdaUpdateWrapper<EventOutbox>()
                .eq(EventOutbox::getId, outboxId)
                .in(EventOutbox::getStatus, OutboxStatuses.PENDING, OutboxStatuses.PROCESSING)
                .set(EventOutbox::getStatus, OutboxStatuses.PROCESSING)
                .set(EventOutbox::getUpdatedAt, now)
                .setSql("attempt_count = COALESCE(attempt_count, 0) + 1"));
        if (claimed == 0) return;

        row = eventOutboxMapper.selectById(outboxId);
        OutboxEventHandler handler = resolveHandler(row.getEventType());
        if (handler == null) {
            throw new IllegalStateException("No OutboxEventHandler for eventType=" + row.getEventType());
        }

        EventOutbox finalRow = row;
        inboxService.tryProcess(outboxId, () -> handler.handle(finalRow));

        eventOutboxMapper.update(null, new LambdaUpdateWrapper<EventOutbox>()
                .eq(EventOutbox::getId, outboxId)
                .set(EventOutbox::getStatus, OutboxStatuses.DONE)
                .set(EventOutbox::getLastError, null)
                .set(EventOutbox::getNextRetryAt, null)
                .set(EventOutbox::getUpdatedAt, System.currentTimeMillis()));
    }

    private void markFailure(Long outboxId, Exception error) {
        EventOutbox row = eventOutboxMapper.selectById(outboxId);
        if (row == null) return;
        // attempt_count may have rolled back with the failed process TX — bump here
        int attempts = (row.getAttemptCount() != null ? row.getAttemptCount() : 0) + 1;
        String message = error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName();
        if (message.length() > 2000) message = message.substring(0, 2000);
        long now = System.currentTimeMillis();
        if (attempts >= maxAttempts) {
            eventOutboxMapper.update(null, new LambdaUpdateWrapper<EventOutbox>()
                    .eq(EventOutbox::getId, outboxId)
                    .set(EventOutbox::getStatus, OutboxStatuses.DEAD)
                    .set(EventOutbox::getAttemptCount, attempts)
                    .set(EventOutbox::getLastError, message)
                    .set(EventOutbox::getUpdatedAt, now));
            log.error("Outbox event moved to DEAD id={} type={} attempts={}", outboxId, row.getEventType(), attempts);
            return;
        }
        long delay = Math.min(300_000L, 1_000L * (1L << Math.min(attempts, 8)));
        eventOutboxMapper.update(null, new LambdaUpdateWrapper<EventOutbox>()
                .eq(EventOutbox::getId, outboxId)
                .set(EventOutbox::getStatus, OutboxStatuses.PENDING)
                .set(EventOutbox::getAttemptCount, attempts)
                .set(EventOutbox::getNextRetryAt, now + delay)
                .set(EventOutbox::getLastError, message)
                .set(EventOutbox::getUpdatedAt, now));
    }

    private OutboxEventHandler resolveHandler(String eventType) {
        if (handlers == null || handlers.isEmpty()) return null;
        for (OutboxEventHandler h : handlers) {
            if (h.supports(eventType)) return h;
        }
        return null;
    }
}
