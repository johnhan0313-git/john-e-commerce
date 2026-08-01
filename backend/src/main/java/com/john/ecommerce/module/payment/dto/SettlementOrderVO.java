package com.john.ecommerce.module.payment.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SettlementOrderVO {
    private Long id;
    private String settlementNo;
    private String direction;
    private String bizType;
    private Long paymentId;
    private Long orderId;
    private Long merchantId;
    private Long shopId;
    private Long amount;
    private String currency;
    private Integer billStatus;
    private Integer status;
    private Map<String, Object> extra;
    private Long createdAt;
}
