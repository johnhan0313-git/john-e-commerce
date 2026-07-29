package com.john.ecommerce.module.activity.entity;

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
@TableName(value = "t_activity_benefit", autoResultMap = true)
public class ActivityBenefit extends BaseEntity {
    private Long activityId;
    private String benefitType;
    private BigDecimal benefitValue;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> config;
    private Integer status;
}
