package com.john.ecommerce.module.fulfillment.port.inventory;

import java.util.List;

public record InventoryLockCommand(Long orderId, Long warehouseId, List<InventoryLine> lines) {}
