package com.john.ecommerce.module.fulfillment.service.inventory;

import com.john.ecommerce.module.fulfillment.port.inventory.InventoryLockCommand;
import com.john.ecommerce.module.fulfillment.port.inventory.InventoryOrderRef;
import com.john.ecommerce.module.fulfillment.port.inventory.InventoryPort;
import com.john.ecommerce.module.fulfillment.port.inventory.InventoryRestoreCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnMissingBean(InventoryService.class)
public class NoOpInventoryFacade implements InventoryPort {

    @Override
    public void lockForOrder(InventoryLockCommand command) {
        log.debug("NoOp lockForOrder orderId={} lines={}",
                command != null ? command.orderId() : null,
                command != null && command.lines() != null ? command.lines().size() : 0);
    }

    @Override
    public void unlockForOrder(InventoryOrderRef orderRef) {
        log.debug("NoOp unlockForOrder orderId={}", orderRef != null ? orderRef.orderId() : null);
    }

    @Override
    public void consumeForOrder(InventoryOrderRef orderRef) {
        log.debug("NoOp consumeForOrder orderId={}", orderRef != null ? orderRef.orderId() : null);
    }

    @Override
    public void restoreForRefund(InventoryRestoreCommand command) {
        log.debug("NoOp restoreForRefund refundId={} orderId={}",
                command != null ? command.refundId() : null,
                command != null ? command.orderId() : null);
    }

    @Override
    public void initOrSetAvailable(Long warehouseId, Long skuId, int qty) {
        log.debug("NoOp initOrSetAvailable warehouseId={} skuId={} qty={}", warehouseId, skuId, qty);
    }
}
