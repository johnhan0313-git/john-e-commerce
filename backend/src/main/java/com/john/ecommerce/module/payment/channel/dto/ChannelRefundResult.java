package com.john.ecommerce.module.payment.channel.dto;

import lombok.Data;

@Data
public class ChannelRefundResult {
    private boolean success;
    private Long refundedAmount;
    private String message;
}
