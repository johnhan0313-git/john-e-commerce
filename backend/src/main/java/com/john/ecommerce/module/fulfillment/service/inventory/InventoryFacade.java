package com.john.ecommerce.module.fulfillment.service.inventory;

import com.john.ecommerce.module.trade.entity.Order;
import com.john.ecommerce.module.trade.entity.OrderItem;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.john.ecommerce.module.trade.entity.Refund;
import com.john.ecommerce.module.trade.entity.RefundItem;

/**
 * 库存门面：销售预占 / 扣减 / 可售查询；履约批次细节封装在实现内。
 */
public interface InventoryFacade {

    int LOCK_STATUS_RELEASED = 0;
    int LOCK_STATUS_LOCKED = 1;
    int LOCK_STATUS_CONSUMED = 2;

    void lockForOrder(Order order, List<OrderItem> items);

    void unlockForOrder(Order order);

    /**
     * 支付成功：将订单锁定库存扣减（locked → 出库），幂等。
     */
    default void consumeForOrder(Order order) {
        // no-op
    }

    default void restoreForRefund(Refund refund, Order order, List<RefundItem> items) {
        // no-op
    }

    /**
     * 确保默认仓对该 SKU 至少有 minAvailable 可售库存。
     */
    default void ensureStock(Long warehouseId, Long skuId, int minAvailable) {
        // no-op
    }

    /**
     * 初始化 / 设置 DEFAULT 批可售数量（建 SKU 用；qty=0 也会落汇总行）。
     */
    default void initOrSetAvailable(Long warehouseId, Long skuId, int qty) {
        // no-op
    }

    default int getAvailable(Long warehouseId, Long skuId) {
        return 0;
    }

    default Map<Long, Integer> getAvailableBatch(Long warehouseId, Collection<Long> skuIds) {
        return Collections.emptyMap();
    }
}
