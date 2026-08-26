package com.john.ecommerce.module.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class FxOrderVO {
    private Long id;
    private String fxNo;
    private Long paymentId;
    private Long orderId;
    private String sellCurrency;
    private String buyCurrency;
    private Long sellAmount;
    private Long buyAmount;
    private BigDecimal exchangeRate;
    private Integer status;
    private String channelRefNo;
    private Long completedAt;
    private Map<String, Object> extra;
    private Long createdAt;
}
