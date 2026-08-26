package com.john.ecommerce.common.outbox;

import java.util.Map;

/**
 * Append durable domain/integration events in the caller's transaction.
 */
public interface OutboxPort {

    /**
     * Insert a PENDING outbox row. When {@code idempotentKey} is non-null and already present,
     * returns the existing row id without inserting again.
     *
     * @return outbox row id
     */
    Long append(Long tenantId,
                String eventType,
                String aggregateType,
                Long aggregateId,
                Map<String, Object> payload,
                String idempotentKey);
}
