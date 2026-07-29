package com.john.ecommerce.module.payment.channel;

import lombok.Data;

@Data
public class RefundResult {
    private boolean success;
    private String channelRefundNo;
}
