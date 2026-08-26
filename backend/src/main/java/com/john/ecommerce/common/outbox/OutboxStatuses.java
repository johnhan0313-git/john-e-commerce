package com.john.ecommerce.common.outbox;

public final class OutboxStatuses {

    public static final int PENDING = 0;
    public static final int PROCESSING = 1;
    public static final int DONE = 2;
    public static final int DEAD = 3;

    private OutboxStatuses() {
    }
}
