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
@TableName(value = "t_activity", autoResultMap = true)
public class Activity extends BaseEntity {
    private String name;
    private String activityType;
    private String title;
    private String subtitle;
    private Long startTime;
    private Long endTime;
    private Long warmUpTime;
    private Integer status;
    private Integer priority;
    private String stackGroup;
    private Boolean stackable;
    private String promoStage;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> ruleConfig;
    private BigDecimal budget;
    private BigDecimal usedBudget;
    private Integer totalQuota;
    private Integer usedQuota;
}
