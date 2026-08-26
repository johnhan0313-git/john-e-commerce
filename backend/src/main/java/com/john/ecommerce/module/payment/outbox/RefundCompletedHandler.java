package com.john.ecommerce.module.payment.outbox;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.outbox.OutboxEventHandler;
import com.john.ecommerce.common.outbox.OutboxEventTypes;
import com.john.ecommerce.common.outbox.entity.EventOutbox;
import com.john.ecommerce.module.fulfillment.port.inventory.InventoryPort;
import com.john.ecommerce.module.fulfillment.port.inventory.InventoryRefundLine;
import com.john.ecommerce.module.fulfillment.port.inventory.InventoryRestoreCommand;
import com.john.ecommerce.module.trade.entity.RefundItem;
import com.john.ecommerce.module.trade.mapper.RefundItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RefundCompleted → restore inventory for unshipped orders (skips already restored lines).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefundCompletedHandler implements OutboxEventHandler {

    private final InventoryPort inventoryPort;
    private final RefundItemMapper refundItemMapper;

    @Override
    public boolean supports(String eventType) {
        return OutboxEventTypes.REFUND_COMPLETED.equals(eventType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(EventOutbox row) {
        Map<String, Object> payload = row.getPayload();
        if (payload == null) return;
        Long refundId = asLong(payload.get("refundId"));
        Long orderId = asLong(payload.get("orderId"));
        Long warehouseId = asLong(payload.get("warehouseId"));
        if (refundId == null || warehouseId == null) {
            log.warn("RefundCompletedHandler missing refundId/warehouseId eventId={}", row.getId());
            return;
        }

        List<InventoryRefundLine> lines = new ArrayList<>();
        List<RefundItem> toMark = new ArrayList<>();

        Object rawLines = payload.get("lines");
        if (rawLines instanceof List<?> list && !list.isEmpty()) {
            for (Object item : list) {
                if (!(item instanceof Map<?, ?> m)) continue;
                Long refundItemId = asLong(m.get("refundItemId"));
                Long skuId = asLong(m.get("skuId"));
                int qty = asInt(m.get("quantity"));
                if (refundItemId == null || skuId == null || qty <= 0) continue;
                RefundItem ri = refundItemMapper.selectById(refundItemId);
                if (ri != null && ri.getStockRestored() != null && ri.getStockRestored() == 1) continue;
                lines.add(new InventoryRefundLine(refundItemId, skuId, qty));
                if (ri != null) toMark.add(ri);
            }
        } else {
            List<RefundItem> items = refundItemMapper.selectList(new LambdaQueryWrapper<RefundItem>()
                    .eq(RefundItem::getRefundId, refundId));
            for (RefundItem item : items) {
                if (item.getStockRestored() != null && item.getStockRestored() == 1) continue;
                int qty = item.getQuantity() != null ? item.getQuantity() : 0;
                if (qty <= 0 || item.getSkuId() == null) continue;
                lines.add(new InventoryRefundLine(item.getId(), item.getSkuId(), qty));
                toMark.add(item);
            }
        }

        if (lines.isEmpty()) {
            log.info("RefundCompletedHandler nothing to restore refundId={}", refundId);
            return;
        }
        inventoryPort.restoreForRefund(new InventoryRestoreCommand(refundId, orderId, warehouseId, lines));
        for (RefundItem item : toMark) {
            item.setStockRestored(1);
            refundItemMapper.updateById(item);
        }
        log.info("RefundCompletedHandler restored {} line(s) refundId={}", lines.size(), refundId);
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

    private static int asInt(Object v) {
        if (v == null) return 0;
        if (v instanceof Number n) return n.intValue();
        if (v instanceof String s && !s.isBlank()) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}
