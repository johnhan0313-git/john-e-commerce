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
@TableName(value = "t_campaign_scope", autoResultMap = true)
public class CampaignScope extends BaseEntity {
    private Long campaignId;
    private Integer scopeType;
    private Long spuId;
    private Long categoryId;
    private Long skuId;
    private BigDecimal activityPrice;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extraConfig;
}
