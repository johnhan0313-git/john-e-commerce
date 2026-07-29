package com.john.ecommerce.module.fulfillment.service.inventory;

import com.john.ecommerce.module.trade.entity.Order;
import com.john.ecommerce.module.trade.entity.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ConditionalOnMissingBean(InventoryService.class)
public class NoOpInventoryFacade implements InventoryFacade {

    @Override
    public void lockForOrder(Order order, List<OrderItem> items) {
        log.debug("NoOp lockForOrder orderNo={} items={}", order.getOrderNo(), items.size());
    }

    @Override
    public void unlockForOrder(Order order) {
        log.debug("NoOp unlockForOrder orderNo={}", order.getOrderNo());
    }
}
