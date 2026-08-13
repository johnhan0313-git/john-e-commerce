package com.john.ecommerce.module.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.merchant.entity.Shop;
import com.john.ecommerce.module.merchant.service.ShopService;
import com.john.ecommerce.module.payment.entity.*;
import com.john.ecommerce.module.payment.enums.SettlementDirection;
import com.john.ecommerce.module.payment.ledger.entity.LedgerAccount;
import com.john.ecommerce.module.payment.ledger.enums.AccountType;
import com.john.ecommerce.module.payment.ledger.service.LedgerService;
import com.john.ecommerce.module.payment.mapper.*;
import com.john.ecommerce.module.payment.util.MoneyUtils;
import com.john.ecommerce.module.trade.entity.Order;
import com.john.ecommerce.module.trade.entity.Refund;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettlementBillService {

    private final SettlementOrderMapper settlementOrderMapper;
    private final SettlementBillMapper settlementBillMapper;
    private final SettlementBillRefMapper settlementBillRefMapper;
    private final SettlementMapper settlementMapper;
    private final SettlementRefMapper settlementRefMapper;
    private final ShopService shopService;
    private final LedgerService ledgerService;

    @Transactional
    public void createSettlementOrder(Payment payment, Order order, PaymentItem paymentItem) {
        if (order.getShopId() == null) {
            throw new BizException("订单缺少店铺，无法结算");
        }
        long amountCents = MoneyUtils.toCents(paymentItem.getAmount());

        SettlementOrder so = new SettlementOrder();
        so.setSettlementNo("SO" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        so.setDirection(SettlementDirection.FORWARD.getCode());
        so.setBizType("PAYMENT");
        so.setPaymentId(payment.getId());
        so.setOrderId(order.getId());
        so.setMerchantId(order.getMerchantId());
        so.setShopId(order.getShopId());
        so.setAmount(amountCents);
        so.setCurrency(payment.getCurrency());
        so.setBillStatus(0);
        so.setStatus(1);
        settlementOrderMapper.insert(so);
    }

    @Transactional
    public SettlementOrder createRefundReversal(Refund refund, Order order) {
        if (refund == null || refund.getId() == null) throw new BizException("退款申请不能为空");
        if (order == null || order.getShopId() == null) throw new BizException("退款订单缺少店铺");
        String idempotentKey = "REFUND:" + refund.getId();
        SettlementOrder existing = settlementOrderMapper.selectOne(new LambdaQueryWrapper<SettlementOrder>()
                .eq(SettlementOrder::getIdempotentKey, idempotentKey)
                .last("LIMIT 1"));
        if (existing != null) return existing;

        SettlementOrder reversal = new SettlementOrder();
        reversal.setSettlementNo("SR" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        reversal.setDirection(SettlementDirection.REVERSE.getCode());
        reversal.setBizType("REFUND");
        reversal.setPaymentId(refund.getPaymentId());
        reversal.setOrderId(order.getId());
        reversal.setMerchantId(order.getMerchantId());
        reversal.setShopId(order.getShopId());
        reversal.setAmount(MoneyUtils.toCents(refund.getAmount()));
        reversal.setCurrency("CNY");
        reversal.setBillStatus(0);
        reversal.setStatus(1);
        reversal.setIdempotentKey(idempotentKey);
        settlementOrderMapper.insert(reversal);
        return reversal;
    }

    @Transactional
    public void postToBill(Long settlementOrderId, Long billId) {
        SettlementOrder so = settlementOrderMapper.selectById(settlementOrderId);
        if (so == null) throw new BizException("结算单不存在");
        SettlementBill bill = settlementBillMapper.selectById(billId);
        if (bill == null) throw new BizException("结算账单不存在");
        if (!Objects.equals(so.getShopId(), bill.getShopId())) {
            throw new BizException("结算单与账单店铺不一致");
        }

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
        if (bill.getShopId() == null) throw new BizException("账单缺少店铺");

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
        settlement.setShopId(bill.getShopId());
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

        if (netAmount > 0) {
            LedgerAccount account = ledgerService.openAccount(
                    "SHOP", bill.getShopId(), AccountType.SHOP_BALANCE.getCode(), bill.getCurrency());
            ledgerService.credit(account.getId(), netAmount,
                    "SETTLEMENT", "SETTLEMENT", settlement.getId(), "店铺结算入账");
        }

        bill.setStatus("SETTLED");
        bill.setSettleStatus(1);
        bill.setPreSettleAmount(netAmount);
        settlementBillMapper.updateById(bill);

        return settlement;
    }

    public Page<SettlementOrder> listOrders(int page, int size, Long shopId, Long merchantId) {
        return settlementOrderMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SettlementOrder>()
                        .eq(shopId != null, SettlementOrder::getShopId, shopId)
                        .eq(merchantId != null, SettlementOrder::getMerchantId, merchantId)
                        .orderByDesc(SettlementOrder::getCreatedAt));
    }

    public Page<SettlementOrder> listOrders(int page, int size, Long merchantId) {
        return listOrders(page, size, null, merchantId);
    }

    public Page<SettlementBill> listBills(int page, int size, Long shopId, Long merchantId) {
        return settlementBillMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SettlementBill>()
                        .eq(shopId != null, SettlementBill::getShopId, shopId)
                        .eq(merchantId != null, SettlementBill::getMerchantId, merchantId)
                        .orderByDesc(SettlementBill::getCreatedAt));
    }

    public Page<SettlementBill> listBills(int page, int size, Long merchantId) {
        return listBills(page, size, null, merchantId);
    }

    public Page<SettlementBill> listBillsForShop(int page, int size, Long shopId) {
        if (shopId == null) throw new BizException("店铺 ID 不能为空");
        return listBills(page, size, shopId, null);
    }

    public Page<SettlementOrder> listOrdersForShop(int page, int size, Long shopId) {
        if (shopId == null) throw new BizException("店铺 ID 不能为空");
        return listOrders(page, size, shopId, null);
    }

    @Transactional
    public SettlementBill createBill(Long shopId) {
        if (shopId == null) throw new BizException("店铺 ID 不能为空");
        Shop shop = shopService.require(shopId);
        SettlementBill bill = new SettlementBill();
        bill.setBillNo("BILL" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        bill.setMerchantId(shop.getMerchantId());
        bill.setShopId(shopId);
        bill.setPayeeType("SHOP");
        bill.setPayeeId(shopId);
        bill.setCurrency("CNY");
        bill.setBillAmount(0L);
        bill.setPreSettleAmount(0L);
        bill.setStatus("OPEN");
        bill.setSettleStatus(0);
        settlementBillMapper.insert(bill);
        return bill;
    }
}
