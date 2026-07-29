package com.john.ecommerce.module-campaign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class CampaignCreateDTO {
    @NotBlank(message = "活动名称不能为空")
    private String name;
    @NotBlank(message = "活动类型不能为空")
    private String campaignType;
    private String title;
    private String subtitle;
    @NotNull(message = "开始时间不能为空")
    private Long startTime;
    @NotNull(message = "结束时间不能为空")
    private Long endTime;
    private Long warmUpTime;
    private Integer priority;
    @NotNull(message = "活动规则不能为空")
    private Map<String, Object> ruleConfig;
    private BigDecimal budget;
    private Integer totalQuota;
    private List<ScopeDTO> scopes;

    @Data
    public static class ScopeDTO {
        private Integer scopeType;
        private Long spuId;
        private Long categoryId;
        private Long skuId;
        private BigDecimal activityPrice;
        private Map<String, Object> extraConfig;
    }
}
