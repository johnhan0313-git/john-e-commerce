package com.john.ecommerce.module.payment.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_payment", autoResultMap = true)
public class Payment extends BaseEntity {
    private String payNo;
    private String methodCode;
    private Long payAccountId;
    private Long channelConfigId;
    private String channelType;
    private String currency;
    private Long parentPaymentId;
    private String orderGroupNo;
    private BigDecimal amount;
    private Integer status;
    private String escrowMode;
    private String escrowStatus;
    private Integer freezeFlg;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> routeTrace;
    private String channelTradeNo;
    private Long paidAt;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;
}
