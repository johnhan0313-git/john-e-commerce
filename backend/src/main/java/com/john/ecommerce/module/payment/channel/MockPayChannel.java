package com.john.ecommerce.module.payment.channel;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;

@Component
public class MockPayChannel implements PayChannel {

    @Override
    public boolean supports(String channelType) {
        return "MOCK".equalsIgnoreCase(channelType);
    }

    @Override
    public PrepayResult prepay(PaymentContext ctx) {
        PrepayResult r = new PrepayResult();
        r.setSuccess(true);
        r.setChannelTradeNo("MOCK-" + UUID.randomUUID().toString().replace("-", ""));
        r.setExtra(Map.of("channel", "MOCK"));
        return r;
    }

    @Override
    public QueryResult query(String payNo) {
        QueryResult r = new QueryResult();
        r.setStatus(2);
        r.setChannelTradeNo(payNo);
        return r;
    }

    @Override
    public RefundResult refund(RefundContext ctx) {
        RefundResult r = new RefundResult();
        r.setSuccess(true);
        r.setChannelRefundNo("MOCK-R-" + UUID.randomUUID().toString().replace("-", ""));
        return r;
    }
}
