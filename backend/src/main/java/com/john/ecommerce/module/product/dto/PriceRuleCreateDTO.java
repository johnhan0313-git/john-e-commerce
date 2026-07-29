package com.john.ecommerce.module.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PriceRuleCreateDTO {
    private Long spuId;
    private Long skuId;
    @NotBlank(message = "规则类型不能为空")
    private String ruleType;
    private Integer minQty;
    @NotNull(message = "价格不能为空")
    private BigDecimal price;
    private Long startTime;
    private Long endTime;
    private Integer status;
}
