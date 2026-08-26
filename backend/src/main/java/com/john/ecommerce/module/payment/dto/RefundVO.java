package com.john.ecommerce.module.payment.dto;

import lombok.Data;

@Data
public class RefundVO {
    private Long id;
    private String refundNo;
    private Long orderId;
    private Long paymentId;
    private Long userId;
    private Long amount;
    private String reason;
    private Integer status;
    private Long refundedAt;
    private String channelRefundNo;
    private Long createdAt;
}
