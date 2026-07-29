package com.john.ecommerce.module.activity.service.engine;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PromoCandidate {
    private Long activityId;
    private String activityType;
    private String stackGroup;
    private Boolean stackable;
    private String promoStage;
    private Integer priority;
    private BigDecimal discountAmount;
    private String description;
}
