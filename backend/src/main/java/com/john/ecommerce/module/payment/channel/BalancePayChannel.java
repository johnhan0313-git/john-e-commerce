package com.john.ecommerce.module.payment.channel;

import com.john.ecommerce.module.payment.ledger.entity.LedgerAccount;
import com.john.ecommerce.module.payment.ledger.service.LedgerService;
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
        long amountCents = ctx.getPayment().getAmount() != null ? ctx.getPayment().getAmount() : 0L;

        LedgerAccount account = ledgerService.openAccount("USER", userId, "USER_BALANCE", "CNY");
        // 余额支付：预下单即扣可用余额（与旧 freezeDebit 语义对齐）
        ledgerService.debit(account.getId(), amountCents, "PAYMENT", "PAYMENT",
                ctx.getPayment().getId(), null);

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
        long amountCents = ctx.getRefundPayment().getAmount() != null ? ctx.getRefundPayment().getAmount() : 0L;

        LedgerAccount account = ledgerService.openAccount("USER", userId, "USER_BALANCE", "CNY");
        ledgerService.credit(account.getId(), amountCents, "REFUND", "REFUND",
                ctx.getRefundPayment().getId(), null);

        RefundResult r = new RefundResult();
        r.setSuccess(true);
        r.setChannelRefundNo("BAL-R-" + UUID.randomUUID().toString().replace("-", ""));
        return r;
    }
}
