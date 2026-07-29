package com.john.ecommerce.module.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.payment.entity.*;
import com.john.ecommerce.module.payment.enums.SettlementDirection;
import com.john.ecommerce.module.payment.mapper.*;
import com.john.ecommerce.module.payment.util.MoneyUtils;
import com.john.ecommerce.module.trade.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettlementBillService {

    private final SettlementOrderMapper settlementOrderMapper;
    private final SettlementOrderItemMapper settlementOrderItemMapper;
    private final SettlementBillMapper settlementBillMapper;
    private final SettlementBillRefMapper settlementBillRefMapper;
    private final SettlementMapper settlementMapper;
    private final SettlementItemMapper settlementItemMapper;
    private final SettlementRefMapper settlementRefMapper;

    @Transactional
    public void createSettlementOrder(Payment payment, Order order, PaymentItem paymentItem) {
        long amountCents = MoneyUtils.toCents(paymentItem.getAmount());

        SettlementOrder so = new SettlementOrder();
        so.setSettlementNo("SO" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        so.setDirection(SettlementDirection.FORWARD.getCode());
        so.setBizType("PAYMENT");
        so.setPaymentId(payment.getId());
        so.setOrderId(order.getId());
        so.setMerchantId(order.getMerchantId());
        so.setAmount(amountCents);
        so.setCurrency(payment.getCurrency());
        so.setBillStatus(0);
        so.setStatus(1);
        settlementOrderMapper.insert(so);
    }

    @Transactional
    public void postToBill(Long settlementOrderId, Long billId) {
        SettlementOrder so = settlementOrderMapper.selectById(settlementOrderId);
        if (so == null) throw new BizException("结算单不存在");
        SettlementBill bill = settlementBillMapper.selectById(billId);
        if (bill == null) throw new BizException("结算账单不存在");

        SettlementBillRef ref = new SettlementBillRef();
        ref.setSettlementBillId(billId);
        ref.setSettlementOrderId(settlementOrderId);
        settlementBillRefMapper.insert(ref);

        bill.setBillAmount(bill.getBillAmount() + so.getAmount());
        settlementBillMapper.updateById(bill);

        so.setBillStatus(1);
        settlementOrderMapper.updateById(so);
    }

    @Transactional
    public Settlement settle(Long billId) {
        SettlementBill bill = settlementBillMapper.selectById(billId);
        if (bill == null) throw new BizException("结算账单不存在");
        if ("SETTLED".equals(bill.getStatus())) throw new BizException("账单已结算");

        List<SettlementBillRef> refs = settlementBillRefMapper.selectList(
                new LambdaQueryWrapper<SettlementBillRef>().eq(SettlementBillRef::getSettlementBillId, billId));

        long netAmount = 0L;
        for (SettlementBillRef ref : refs) {
            SettlementOrder so = settlementOrderMapper.selectById(ref.getSettlementOrderId());
            if (so != null) {
                netAmount += "FORWARD".equals(so.getDirection()) ? so.getAmount() : -so.getAmount();
            }
        }

        Settlement settlement = new Settlement();
        settlement.setSettleNo("STL" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        settlement.setSettlementBillId(billId);
        settlement.setMerchantId(bill.getMerchantId());
        settlement.setNetAmount(netAmount);
        settlement.setCurrency(bill.getCurrency());
        settlement.setStatus(1);
        settlement.setSettledAt(System.currentTimeMillis());
        settlementMapper.insert(settlement);

        for (SettlementBillRef ref : refs) {
            SettlementRef sref = new SettlementRef();
            sref.setSettlementId(settlement.getId());
            sref.setSettlementOrderId(ref.getSettlementOrderId());
            settlementRefMapper.insert(sref);
        }

        bill.setStatus("SETTLED");
        bill.setSettleStatus(1);
        bill.setPreSettleAmount(netAmount);
        settlementBillMapper.updateById(bill);

        return settlement;
    }

    public Page<SettlementOrder> listOrders(int page, int size, Long merchantId) {
        return settlementOrderMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SettlementOrder>()
                        .eq(merchantId != null, SettlementOrder::getMerchantId, merchantId)
                        .orderByDesc(SettlementOrder::getCreatedAt));
    }

    public Page<SettlementBill> listBills(int page, int size, Long merchantId) {
        return settlementBillMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SettlementBill>()
                        .eq(merchantId != null, SettlementBill::getMerchantId, merchantId)
                        .orderByDesc(SettlementBill::getCreatedAt));
    }

    @Transactional
    public SettlementBill createBill(Long merchantId) {
        SettlementBill bill = new SettlementBill();
        bill.setBillNo("BILL" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        bill.setMerchantId(merchantId);
        bill.setCurrency("CNY");
        bill.setBillAmount(0L);
        bill.setPreSettleAmount(0L);
        bill.setStatus("OPEN");
        bill.setSettleStatus(0);
        settlementBillMapper.insert(bill);
        return bill;
    }
}
