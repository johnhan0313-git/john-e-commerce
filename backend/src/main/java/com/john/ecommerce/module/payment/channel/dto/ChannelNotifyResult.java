package com.john.ecommerce.module.payment.channel.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChannelNotifyResult {
    private boolean success;
    private String payNo;
    private String channelTradeNo;
    private Long amount;
    private String message;
}
