package com.john.ecommerce.common.outbox;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.common.outbox.entity.EventOutbox;
import com.john.ecommerce.common.outbox.mapper.EventOutboxMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OutboxService implements OutboxPort {

    private final EventOutboxMapper eventOutboxMapper;

    @Override
    @Transactional
    public Long append(Long tenantId,
                       String eventType,
                       String aggregateType,
                       Long aggregateId,
                       Map<String, Object> payload,
                       String idempotentKey) {
        if (tenantId == null) throw new BizException("租户不能为空");
        if (eventType == null || eventType.isBlank()) throw new BizException("事件类型不能为空");
        if (aggregateType == null || aggregateType.isBlank()) throw new BizException("聚合类型不能为空");
        if (aggregateId == null) throw new BizException("聚合ID不能为空");

        if (idempotentKey != null && !idempotentKey.isBlank()) {
            EventOutbox existing = eventOutboxMapper.selectOne(new LambdaQueryWrapper<EventOutbox>()
                    .eq(EventOutbox::getIdempotentKey, idempotentKey)
                    .last("LIMIT 1"));
            if (existing != null) {
                return existing.getId();
            }
        }

        EventOutbox row = new EventOutbox();
        row.setTenantId(tenantId);
        row.setEventType(eventType);
        row.setAggregateType(aggregateType);
        row.setAggregateId(aggregateId);
        row.setPayload(payload != null ? new HashMap<>(payload) : new HashMap<>());
        row.setStatus(OutboxStatuses.PENDING);
        row.setAttemptCount(0);
        row.setIdempotentKey(idempotentKey);
        eventOutboxMapper.insert(row);
        return row.getId();
    }
}
