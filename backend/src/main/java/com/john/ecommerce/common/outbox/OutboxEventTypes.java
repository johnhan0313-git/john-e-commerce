package com.john.ecommerce.common.outbox;

public final class OutboxEventTypes {

    public static final String ORDER_PAID = "OrderPaid";
    public static final String REFUND_COMPLETED = "RefundCompleted";

    private OutboxEventTypes() {
    }
}
