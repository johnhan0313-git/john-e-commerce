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
}
