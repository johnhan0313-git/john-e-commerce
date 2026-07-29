package com.john.ecommerce.module.payment.channel;

import com.john.ecommerce.module.payment.entity.Payment;
import com.john.ecommerce.module.payment.entity.PayChannelConfig;
import lombok.Data;

@Data
public class RefundContext {
    private Payment payment;
    private Payment refundPayment;
    private PayChannelConfig config;
}
