package com.john.ecommerce.common.outbox;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.common.outbox.entity.EventInbox;
import com.john.ecommerce.common.outbox.entity.EventOutbox;
import com.john.ecommerce.common.outbox.mapper.EventInboxMapper;
import com.john.ecommerce.common.outbox.mapper.EventOutboxMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

    @Mock
    EventOutboxMapper eventOutboxMapper;

    @InjectMocks
    OutboxService outboxService;

    @Test
    void appendInsertsPendingRow() {
        when(eventOutboxMapper.insert(any(EventOutbox.class))).thenAnswer(inv -> {
            EventOutbox row = inv.getArgument(0);
            row.setId(100L);
            return 1;
        });

        Long id = outboxService.append(1L, OutboxEventTypes.ORDER_PAID, "Order", 9L,
                Map.of("orderId", 9L, "warehouseId", 1L), "OrderPaid:9");

        assertThat(id).isEqualTo(100L);
        ArgumentCaptor<EventOutbox> captor = ArgumentCaptor.forClass(EventOutbox.class);
        verify(eventOutboxMapper).insert(captor.capture());
        EventOutbox row = captor.getValue();
        assertThat(row.getTenantId()).isEqualTo(1L);
        assertThat(row.getEventType()).isEqualTo(OutboxEventTypes.ORDER_PAID);
        assertThat(row.getAggregateType()).isEqualTo("Order");
        assertThat(row.getAggregateId()).isEqualTo(9L);
        assertThat(row.getStatus()).isEqualTo(OutboxStatuses.PENDING);
        assertThat(row.getAttemptCount()).isZero();
        assertThat(row.getIdempotentKey()).isEqualTo("OrderPaid:9");
        assertThat(row.getPayload()).containsEntry("orderId", 9L);
    }

    @Test
    void appendReturnsExistingWhenIdempotentKeyMatches() {
        EventOutbox existing = new EventOutbox();
        existing.setId(55L);
        existing.setIdempotentKey("OrderPaid:9");
        when(eventOutboxMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        Long id = outboxService.append(1L, OutboxEventTypes.ORDER_PAID, "Order", 9L,
                Map.of("orderId", 9L), "OrderPaid:9");

        assertThat(id).isEqualTo(55L);
        verify(eventOutboxMapper, never()).insert(any(EventOutbox.class));
    }

    @Test
    void appendRejectsNullTenant() {
        assertThatThrownBy(() -> outboxService.append(null, "X", "Order", 1L, Map.of(), null))
                .isInstanceOf(BizException.class);
    }
}

@ExtendWith(MockitoExtension.class)
class InboxServiceTest {

    @Mock
    EventInboxMapper eventInboxMapper;

    @InjectMocks
    InboxService inboxService;

    @Test
    void tryProcessRunsOnceAndInsertsInbox() {
        when(eventInboxMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(eventInboxMapper.insert(any(EventInbox.class))).thenReturn(1);
        AtomicInteger runs = new AtomicInteger();

        boolean first = inboxService.tryProcess(10L, runs::incrementAndGet);
        assertThat(first).isTrue();
        assertThat(runs.get()).isEqualTo(1);
        verify(eventInboxMapper).insert(any(EventInbox.class));
    }

    @Test
    void tryProcessSkipsWhenAlreadyProcessed() {
        when(eventInboxMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        AtomicInteger runs = new AtomicInteger();

        boolean again = inboxService.tryProcess(10L, runs::incrementAndGet);
        assertThat(again).isFalse();
        assertThat(runs.get()).isZero();
        verify(eventInboxMapper, never()).insert(any(EventInbox.class));
    }
}
