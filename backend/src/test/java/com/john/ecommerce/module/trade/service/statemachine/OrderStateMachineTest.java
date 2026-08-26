package com.john.ecommerce.module.trade.service.statemachine;

import com.john.ecommerce.common.enums.OrderStatus;
import com.john.ecommerce.common.exception.BizException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStateMachineTest {

    private final OrderStateMachine sm = new OrderStateMachine();

    @Test
    void allowsPendingToPaidAndCancelled() {
        assertThat(sm.canTransition(OrderStatus.PENDING.getCode(), OrderStatus.PAID.getCode())).isTrue();
        assertThat(sm.canTransition(OrderStatus.PENDING.getCode(), OrderStatus.CANCELLED.getCode())).isTrue();
    }

    @Test
    void rejectsIllegalTransitions() {
        assertThat(sm.canTransition(OrderStatus.PENDING.getCode(), OrderStatus.SHIPPED.getCode())).isFalse();
        assertThatThrownBy(() -> sm.assertTransition(OrderStatus.CANCELLED.getCode(), OrderStatus.PAID.getCode()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("非法状态流转");
    }

    @Test
    void paidCanShipOrRefund() {
        assertThat(sm.canTransition(OrderStatus.PAID.getCode(), OrderStatus.SHIPPED.getCode())).isTrue();
        assertThat(sm.canTransition(OrderStatus.PAID.getCode(), OrderStatus.PARTIAL_SHIPPED.getCode())).isTrue();
        assertThat(sm.canTransition(OrderStatus.PAID.getCode(), OrderStatus.REFUNDING.getCode())).isTrue();
    }

    @Test
    void refundingCanRestoreFulfillmentStates() {
        assertThat(sm.canTransition(OrderStatus.REFUNDING.getCode(), OrderStatus.REFUNDED.getCode())).isTrue();
        assertThat(sm.canTransition(OrderStatus.REFUNDING.getCode(), OrderStatus.SHIPPED.getCode())).isTrue();
        assertThat(sm.canTransition(OrderStatus.REFUNDING.getCode(), OrderStatus.PARTIAL_SHIPPED.getCode())).isTrue();
    }
}
