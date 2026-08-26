package com.john.ecommerce.module.activity.service.engine;

import lombok.Data;

@Data
public class PromoCandidate {
    private Long activityId;
    private String activityType;
    private String stackGroup;
    private Boolean stackable;
    private String promoStage;
    private Integer priority;
    private Long discountAmount;
    private String description;
}
