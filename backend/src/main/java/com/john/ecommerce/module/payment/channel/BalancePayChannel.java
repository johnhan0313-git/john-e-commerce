package com.john.ecommerce.module.payment.channel;

import com.john.ecommerce.module.payment.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BalancePayChannel implements PayChannel {

    private final LedgerService ledgerService;

    @Override
    public boolean supports(String channelType) {
        return "BALANCE".equalsIgnoreCase(channelType);
    }

    @Override
    public PrepayResult prepay(PaymentContext ctx) {
        Long userId = ctx.getPayment().getCreatedBy();
        Long tenantId = ctx.getPayment().getTenantId();
        long amountCents = ctx.getPayment().getAmount().movePointRight(2).longValue();

        ledgerService.freezeDebit(tenantId, "USER_BALANCE", userId, amountCents,
                "PAYMENT", ctx.getPayment().getId());

        PrepayResult r = new PrepayResult();
        r.setSuccess(true);
        r.setChannelTradeNo("BAL-" + UUID.randomUUID().toString().replace("-", ""));
        r.setExtra(Map.of("channel", "BALANCE"));
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
        Long userId = ctx.getPayment().getCreatedBy();
        Long tenantId = ctx.getPayment().getTenantId();
        long amountCents = ctx.getRefundPayment().getAmount().movePointRight(2).longValue();

        ledgerService.credit(tenantId, "USER_BALANCE", userId, amountCents,
                "REFUND", ctx.getRefundPayment().getId());

        RefundResult r = new RefundResult();
        r.setSuccess(true);
        r.setChannelRefundNo("BAL-R-" + UUID.randomUUID().toString().replace("-", ""));
        return r;
    }
}
