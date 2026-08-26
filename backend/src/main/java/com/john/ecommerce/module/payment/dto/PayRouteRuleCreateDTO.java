package com.john.ecommerce.module.payment.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PayRouteRuleCreateDTO {
    private String methodCode;
    private String scene;
    private Long payAccountId;
    private String channelType;
    private Map<String, Object> condition;
    private Integer priority;
    private Integer status;
}
