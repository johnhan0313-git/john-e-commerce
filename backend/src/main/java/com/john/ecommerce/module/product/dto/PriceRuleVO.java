package com.john.ecommerce.module.product.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PriceRuleVO {
    private Long id;
    private Long spuId;
    private Long skuId;
    private String ruleType;
    private Integer minQty;
    private Long price;
    private Long startTime;
    private Long endTime;
    private Integer status;
    private Long createdAt;
}
