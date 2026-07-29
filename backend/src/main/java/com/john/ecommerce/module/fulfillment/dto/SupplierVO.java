package com.john.ecommerce.module.fulfillment.dto;

import lombok.Data;

@Data
public class SupplierVO {
    private Long id;
    private String name;
    private String contactName;
    private String contactPhone;
    private String address;
    private Integer status;
    private Long createdAt;
}
