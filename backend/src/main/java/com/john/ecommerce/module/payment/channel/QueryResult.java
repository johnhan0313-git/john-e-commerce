package com.john.ecommerce.module.payment.channel;

import lombok.Data;

@Data
public class QueryResult {
    private int status;
    private String channelTradeNo;
}
