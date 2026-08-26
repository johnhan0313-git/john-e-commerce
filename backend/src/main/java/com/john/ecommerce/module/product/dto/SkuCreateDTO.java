package com.john.ecommerce.module.product.dto;

import jakarta.validation.constraints.Min;
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
    private Long price;
    private Long costPrice;
    private Boolean lotEnabled;
    private BigDecimal weight;
    private String barcode;
    private Integer status;
    /** 初始可售库存（默认仓）；null/未传视为 0 */
    @Min(value = 0, message = "初始库存不能为负")
    private Integer initStock;
}
