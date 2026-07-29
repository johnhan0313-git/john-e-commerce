package com.john.ecommerce.module.fulfillment.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_logistics_order", autoResultMap = true)
public class LogisticsOrder extends BaseEntity {
    private Long orderId;
    private String logisticsNo;
    private String provider;
    private String trackingNo;
    /** 0=created 1=shipped 2=delivered */
    private Integer status;
    private Long shippedAt;
    private Long deliveredAt;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;
}
