package com.john.ecommerce.module.trade.service.statemachine;

import com.john.ecommerce.common.enums.OrderStatus;
import com.john.ecommerce.common.exception.BizException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class OrderStateMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        TRANSITIONS.put(OrderStatus.PENDING, EnumSet.of(OrderStatus.PAID, OrderStatus.CANCELLED));
        TRANSITIONS.put(OrderStatus.PAID, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.PARTIAL_SHIPPED, OrderStatus.REFUNDING, OrderStatus.CANCELLED));
        TRANSITIONS.put(OrderStatus.PARTIAL_SHIPPED, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.DELIVERED, OrderStatus.REFUNDING));
        TRANSITIONS.put(OrderStatus.SHIPPED, EnumSet.of(OrderStatus.DELIVERED, OrderStatus.REFUNDING));
        TRANSITIONS.put(OrderStatus.DELIVERED, EnumSet.of(OrderStatus.COMPLETED, OrderStatus.REFUNDING));
        TRANSITIONS.put(OrderStatus.COMPLETED, EnumSet.of(OrderStatus.REFUNDING));
        TRANSITIONS.put(OrderStatus.REFUNDING, EnumSet.of(OrderStatus.REFUNDED, OrderStatus.PAID, OrderStatus.COMPLETED));
        TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
        TRANSITIONS.put(OrderStatus.REFUNDED, EnumSet.noneOf(OrderStatus.class));
    }

    public void assertTransition(int fromStatus, int toStatus) {
        OrderStatus from = OrderStatus.of(fromStatus);
        OrderStatus to = OrderStatus.of(toStatus);
        Set<OrderStatus> allowed = TRANSITIONS.getOrDefault(from, EnumSet.noneOf(OrderStatus.class));
        if (!allowed.contains(to)) {
            throw new BizException("非法状态流转: " + from.getLabel() + " -> " + to.getLabel());
        }
    }

    public boolean canTransition(int fromStatus, int toStatus) {
        try {
            assertTransition(fromStatus, toStatus);
            return true;
        } catch (BizException e) {
            return false;
        }
    }
}
