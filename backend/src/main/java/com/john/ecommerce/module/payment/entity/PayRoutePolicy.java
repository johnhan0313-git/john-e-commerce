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
@TableName(value = "t_pay_route_policy", autoResultMap = true)
public class PayRoutePolicy extends BaseEntity {
    private String name;
    private String strategyType;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> config;
    private Integer status;
}
