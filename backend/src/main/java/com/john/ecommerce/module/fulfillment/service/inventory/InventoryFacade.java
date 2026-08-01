package com.john.ecommerce.module.fulfillment.service.inventory;

import com.john.ecommerce.module.trade.entity.Order;
import com.john.ecommerce.module.trade.entity.OrderItem;

import java.util.List;

/**
 * 库存锁定门面；履约模块就绪后由实现类替换 no-op。
 */
public interface InventoryFacade {

    void lockForOrder(Order order, List<OrderItem> items);

    void unlockForOrder(Order order);

    /**
     * 确保默认仓对该 SKU 至少有 minAvailable 可售库存（演示/新建 SKU 用）。
     */
    default void ensureStock(Long warehouseId, Long skuId, int minAvailable) {
        // no-op
    }
}
