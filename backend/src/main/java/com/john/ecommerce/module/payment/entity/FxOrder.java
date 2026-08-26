package com.john.ecommerce.module.payment.entity;

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
@TableName(value = "t_fx_order", autoResultMap = true)
public class FxOrder extends BaseEntity {
    private String fxNo;
    private Long paymentId;
    private Long orderId;
    private String sellCurrency;
    private String buyCurrency;
    private Long sellAmount;
    private Long buyAmount;
    private BigDecimal exchangeRate;
    private Integer status;
    private String channelRefNo;
    private Long completedAt;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;
}
