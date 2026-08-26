package com.john.ecommerce.module.fulfillment.port.inventory;

public record InventoryRefundLine(Long refundItemId, Long skuId, int quantity) {}
