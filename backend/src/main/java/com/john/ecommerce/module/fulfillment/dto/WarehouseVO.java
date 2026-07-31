package com.john.ecommerce.module.fulfillment.dto;

import lombok.Data;

@Data
public class WarehouseVO {
    private Long id;
    private Long merchantId;
    private Long shopId;
    private String code;
    private String name;
    private String address;
    private Integer status;
    private Long createdAt;
}
