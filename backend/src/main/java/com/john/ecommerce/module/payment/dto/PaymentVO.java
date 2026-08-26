package com.john.ecommerce.module.payment.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class PaymentVO {
    private Long id;
    private String payNo;
    private String methodCode;
    private String channelType;
    private String currency;
    private Long amount;
    private Integer status;
    private String channelTradeNo;
    private Long paidAt;
    private Map<String, Object> extra;
    private List<PaymentItemVO> items;
    private Long createdAt;

    @Data
    public static class PaymentItemVO {
        private Long id;
        private Long orderId;
        private Long amount;
    }
}
