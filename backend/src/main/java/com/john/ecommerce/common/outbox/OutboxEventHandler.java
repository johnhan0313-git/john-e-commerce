package com.john.ecommerce.common.outbox;

import com.john.ecommerce.common.outbox.entity.EventOutbox;

public interface OutboxEventHandler {

    boolean supports(String eventType);

    void handle(EventOutbox row);
}
