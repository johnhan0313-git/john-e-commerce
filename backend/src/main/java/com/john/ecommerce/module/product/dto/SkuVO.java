package com.john.ecommerce.module.product.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class SkuVO {
    private Long id;
    private Long spuId;
    private String skuCode;
    private String skuName;
    private Map<String, String> specValues;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Boolean lotEnabled;
    private BigDecimal weight;
    private String barcode;
    private Integer status;
    /** 默认仓可售库存 */
    private Integer available;
    private Long createdAt;
}
