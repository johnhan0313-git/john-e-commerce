package com.john.ecommerce.module.payment.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_settlement_order", autoResultMap = true)
public class SettlementOrder extends BaseEntity {
    private String settlementNo;
    private String direction;
    private String bizType;
    private Long paymentId;
    private Long orderId;
    private Long merchantId;
    private Long shopId;
    private Long amount;
    private String currency;
    private Integer billStatus;
    private Integer status;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;
}
