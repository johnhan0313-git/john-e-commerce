package com.john.ecommerce.module.product.dto;

import lombok.Data;

@Data
public class CartVO {
    private Long id;
    private Long skuId;
    private String skuName;
    private Long spuId;
    private String spuName;
    private Integer quantity;
    private Integer selected;
    private Long createdAt;
}
