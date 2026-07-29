package com.john.ecommerce.module.fulfillment.dto;

import lombok.Data;

@Data
public class WarehouseStockVO {
    private Long id;
    private Long warehouseId;
    private Long skuId;
    private Integer available;
    private Integer locked;
    private Integer inTransit;
}
