package com.john.ecommerce.module.fulfillment.port.inventory;

import java.util.List;

public record InventoryRestoreCommand(Long refundId, Long orderId, Long warehouseId, List<InventoryRefundLine> lines) {}
