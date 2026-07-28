package com.john.ecommerce.module-product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_sku", autoResultMap = true)
public class Sku extends BaseEntity {
    private Long spuId;
    private String skuName;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> specValues;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer stock;
    private BigDecimal weight;
    private String barcode;
    private Integer status;
}
