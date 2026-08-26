package com.john.ecommerce.module.payment.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PayAccountVO {
    private Long id;
    private String accountCode;
    private String name;
    private String ownerType;
    private Long ownerId;
    private String currency;
    private Long defaultRoutePolicyId;
    private Integer status;
    private Map<String, Object> extra;
    private Long createdAt;
}
