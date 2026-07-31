package com.john.ecommerce.module.merchant.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ShopVO {
    private Long id;
    private Long merchantId;
    private String name;
    private String logo;
    private Integer status;
    private String statusLabel;
    private Map<String, Object> extra;
    private Long createdAt;
}
