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
@TableName(value = "t_pay_channel_config", autoResultMap = true)
public class PayChannelConfig extends BaseEntity {
    private Long payAccountId;
    private String channelType;
    private String mchNo;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> credentials;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> capability;
    private Integer weight;
    private Integer status;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;
}
