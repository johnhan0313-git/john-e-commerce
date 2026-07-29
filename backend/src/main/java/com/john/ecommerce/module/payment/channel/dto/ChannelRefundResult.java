package com.john.ecommerce.module.payment.channel.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChannelRefundResult {
    private boolean success;
    private BigDecimal refundedAmount;
    private String message;
}
