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
@TableName(value = "t_pay_route_rule", autoResultMap = true)
public class PayRouteRule extends BaseEntity {
    private String methodCode;
    private String scene;
    private Long payAccountId;
    private String channelType;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> condition;
    private Integer priority;
    private Integer status;
}
