package com.john.ecommerce.module.payment.channel.dto;

import lombok.Data;

@Data
public class ChannelQueryResult {
    private boolean paid;
    private String channelTradeNo;
    private String message;
}
