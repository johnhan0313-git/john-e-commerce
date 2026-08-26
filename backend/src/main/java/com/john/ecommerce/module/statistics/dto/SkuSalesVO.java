package com.john.ecommerce.module.statistics.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuSalesVO {
    private Long skuId;
    private String skuName;
    private Integer totalQty;
    private Long totalAmount;
}
