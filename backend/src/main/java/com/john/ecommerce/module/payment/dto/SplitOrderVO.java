package com.john.ecommerce.module.payment.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SplitOrderVO {
    private Long id;
    private String splitNo;
    private Long paymentId;
    private Long settlementId;
    private String channelType;
    private String channelSplitNo;
    private Long totalAmount;
    private Integer status;
    private Long confirmedAt;
    private Map<String, Object> extra;
    private Long createdAt;
}
