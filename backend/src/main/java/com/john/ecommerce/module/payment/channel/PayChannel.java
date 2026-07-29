package com.john.ecommerce.module.payment.channel;

public interface PayChannel {
    boolean supports(String channelType);
    PrepayResult prepay(PaymentContext ctx);
    QueryResult query(String payNo);
    RefundResult refund(RefundContext ctx);
}
