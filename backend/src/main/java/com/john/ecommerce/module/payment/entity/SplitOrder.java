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
@TableName(value = "t_split_order", autoResultMap = true)
public class SplitOrder extends BaseEntity {
    private String splitNo;
    private Long paymentId;
    private Long settlementId;
    private String channelType;
    private String channelSplitNo;
    private Long totalAmount;
    private Integer status;
    private Long confirmedAt;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;
}
