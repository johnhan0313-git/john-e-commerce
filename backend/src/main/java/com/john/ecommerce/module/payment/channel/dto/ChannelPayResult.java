package com.john.ecommerce.module.payment.channel.dto;

import lombok.Data;

@Data
public class ChannelPayResult {
    private boolean success;
    private String channelTradeNo;
    private String prepayData;
    private String message;

    public static ChannelPayResult success(String channelTradeNo, String prepayData) {
        ChannelPayResult r = new ChannelPayResult();
        r.setSuccess(true);
        r.setChannelTradeNo(channelTradeNo);
        r.setPrepayData(prepayData);
        return r;
    }

    public static ChannelPayResult fail(String message) {
        ChannelPayResult r = new ChannelPayResult();
        r.setSuccess(false);
        r.setMessage(message);
        return r;
    }
}
