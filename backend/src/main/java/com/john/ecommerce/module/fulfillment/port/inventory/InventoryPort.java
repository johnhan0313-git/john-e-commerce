package com.john.ecommerce.module.fulfillment.port.inventory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * 库存门面：销售预占 / 扣减 / 可售查询；履约批次细节封装在实现内。
 * 跨域调用只传 command / ref DTO，不传 trade 实体。
 */
public interface InventoryPort {

    int LOCK_STATUS_RELEASED = 0;
    int LOCK_STATUS_LOCKED = 1;
    int LOCK_STATUS_CONSUMED = 2;

    void lockForOrder(InventoryLockCommand command);

    void unlockForOrder(InventoryOrderRef orderRef);

    /**
     * 支付成功：将订单锁定库存扣减（locked → 出库），幂等。
     */
    default void consumeForOrder(InventoryOrderRef orderRef) {
        // no-op
    }

    default void restoreForRefund(InventoryRestoreCommand command) {
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
