package com.john.ecommerce.module-trade.entity;

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
@TableName(value = "t_order_item", autoResultMap = true)
public class OrderItem extends BaseEntity {
    private Long orderId;
    private Long spuId;
    private Long skuId;
    private String skuName;
    private String skuImage;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> specValues;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
