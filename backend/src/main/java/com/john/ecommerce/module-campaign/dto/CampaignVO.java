package com.john.ecommerce.module-campaign.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class CampaignVO {
    private Long id;
    private String name;
    private String campaignType;
    private String campaignTypeLabel;
    private String title;
    private String subtitle;
    private Long startTime;
    private Long endTime;
    private Long warmUpTime;
    private Integer status;
    private String statusLabel;
    private Integer priority;
    private Map<String, Object> ruleConfig;
    private BigDecimal budget;
    private BigDecimal usedBudget;
    private Integer totalQuota;
    private Integer usedQuota;
    private List<ScopeVO> scopes;
    private Long createdAt;

    @Data
    public static class ScopeVO {
        private Long id;
        private Integer scopeType;
        private Long spuId;
        private Long categoryId;
        private Long skuId;
        private BigDecimal activityPrice;
    }
}
