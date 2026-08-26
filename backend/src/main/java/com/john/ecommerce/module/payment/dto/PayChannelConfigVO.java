package com.john.ecommerce.module.payment.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PayChannelConfigVO {
    private Long id;
    private Long payAccountId;
    private String channelType;
    private String mchNo;
    private Map<String, Object> credentials;
    private Map<String, Object> capability;
    private Integer weight;
    private Integer status;
    private Map<String, Object> extra;
    private Long createdAt;
}
