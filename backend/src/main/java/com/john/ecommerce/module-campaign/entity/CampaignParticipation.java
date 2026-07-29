package com.john.ecommerce.module-campaign.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_campaign_participation", autoResultMap = true)
public class CampaignParticipation extends BaseEntity {
    private Long campaignId;
    private Long userId;
    private String participateType;
    private Long refOrderId;
    private Long refGroupId;
    private Integer status;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;
}
