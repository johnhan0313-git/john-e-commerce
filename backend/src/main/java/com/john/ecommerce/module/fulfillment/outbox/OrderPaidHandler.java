package com.john.ecommerce.module.fulfillment.outbox;

import com.john.ecommerce.common.outbox.OutboxEventHandler;
import com.john.ecommerce.common.outbox.OutboxEventTypes;
import com.john.ecommerce.common.outbox.entity.EventOutbox;
import com.john.ecommerce.module.fulfillment.port.inventory.InventoryOrderRef;
import com.john.ecommerce.module.fulfillment.port.inventory.InventoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * OrderPaid → consume locked inventory (idempotent via lock detail status).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidHandler implements OutboxEventHandler {

    private final InventoryPort inventoryPort;

    @Override
    public boolean supports(String eventType) {
        return OutboxEventTypes.ORDER_PAID.equals(eventType);
    }

    @Override
    public void handle(EventOutbox row) {
        Map<String, Object> payload = row.getPayload();
        Long orderId = asLong(payload != null ? payload.get("orderId") : null);
        if (orderId == null) {
            orderId = row.getAggregateId();
        }
        Long warehouseId = asLong(payload != null ? payload.get("warehouseId") : null);
        log.info("OrderPaidHandler consume inventory orderId={} warehouseId={}", orderId, warehouseId);
        inventoryPort.consumeForOrder(new InventoryOrderRef(orderId, warehouseId));
    }

    private static Long asLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        if (v instanceof String s && !s.isBlank()) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
