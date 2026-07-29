package com.john.ecommerce.module.payment.channel;

import lombok.Data;
import java.util.Map;

@Data
public class PrepayResult {
    private boolean success;
    private String channelTradeNo;
    private Map<String, Object> extra;
}
