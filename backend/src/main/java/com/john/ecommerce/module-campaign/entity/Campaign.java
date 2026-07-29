package com.john.ecommerce.module-campaign.entity;

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
@TableName(value = "t_campaign", autoResultMap = true)
public class Campaign extends BaseEntity {
    private String name;
    private String campaignType;
    private String title;
    private String subtitle;
    private Long startTime;
    private Long endTime;
    private Long warmUpTime;
    private Integer status;
    private Integer priority;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> ruleConfig;
    private BigDecimal budget;
    private BigDecimal usedBudget;
    private Integer totalQuota;
    private Integer usedQuota;
}
