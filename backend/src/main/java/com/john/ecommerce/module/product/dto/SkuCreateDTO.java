package com.john.ecommerce.module.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class SkuCreateDTO {
    @NotNull(message = "SPU ID 不能为空")
    private Long spuId;
    private String skuCode;
    private String skuName;
    private Map<String, String> specValues;
    @NotNull(message = "价格不能为空")
    private BigDecimal price;
    private BigDecimal costPrice;
    private Boolean lotEnabled;
    private BigDecimal weight;
    private String barcode;
    private Integer status;
}
