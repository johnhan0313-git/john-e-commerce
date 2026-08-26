package com.john.ecommerce.module.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/** 创建/编辑 SPU 时一并提交的 SKU 行（无需 spuId；编辑时带 id） */
@Data
public class SkuItemDTO {
    /** 已有 SKU 的 id；新建不传 */
    private Long id;
    private String skuCode;
    private String skuName;
    private Map<String, String> specValues;
    @NotNull(message = "价格不能为空")
    private Long price;
    private Long costPrice;
    /** 创建=初始库存；编辑=覆盖可售库存（传则更新） */
    @Min(value = 0, message = "库存不能为负")
    private Integer initStock;
    private Integer status;
}
