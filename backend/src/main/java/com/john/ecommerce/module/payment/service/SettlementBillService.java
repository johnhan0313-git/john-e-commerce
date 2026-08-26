package com.john.ecommerce.module.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.merchant.entity.Shop;
import com.john.ecommerce.module.merchant.service.ShopService;
import com.john.ecommerce.module.payment.dto.SettlementBillVO;
import com.john.ecommerce.module.payment.dto.SettlementOrderVO;
import com.john.ecommerce.module.payment.dto.SettlementVO;
import com.john.ecommerce.module.payment.entity.*;
import com.john.ecommerce.module.payment.enums.SettlementDirection;
import com.john.ecommerce.module.payment.ledger.entity.LedgerAccount;
import com.john.ecommerce.module.payment.ledger.enums.AccountType;
import com.john.ecommerce.module.payment.ledger.service.LedgerService;
import com.john.ecommerce.module.payment.mapper.*;
import com.john.ecommerce.module.trade.entity.Order;
import com.john.ecommerce.module.trade.entity.Refund;
import com.john.ecommerce.module.trade.mapper.OrderMapper;
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
    private final SettlementOrderItemMapper settlementOrderItemMapper;
    private final SettlementBillMapper settlementBillMapper;
    private final SettlementBillRefMapper settlementBillRefMapper;
    private final SettlementMapper settlementMapper;
    private final SettlementRefMapper settlementRefMapper;
    private final ShopService shopService;
    private final LedgerService ledgerService;
    private final OrderMapper orderMapper;

    /**
     * Create FORWARD settlement order for a payment item, write SettlementOrderItem,
     * and auto-post into the open shop bill.
     */
    @Transactional
    public void createSettlementOrder(Payment payment, PaymentItem item) {
        Order order = orderMapper.selectById(item.getOrderId());
        Long merchantId = order != null ? order.getMerchantId() : null;
        Long shopId = order != null ? order.getShopId() : null;
        if (shopId == null) {
            throw new BizException("订单缺少店铺，无法结算");
        }
        long amountCents = item.getAmount() != null ? item.getAmount() : 0L;

        SettlementOrder so = new SettlementOrder();
        so.setSettlementNo("SO" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        so.setDirection(SettlementDirection.FORWARD.getCode());
        so.setBizType("PAYMENT");
        so.setPaymentId(payment.getId());
        so.setOrderId(item.getOrderId());
        so.setMerchantId(merchantId);
        so.setShopId(shopId);
        so.setAmount(amountCents);
        so.setCurrency(payment.getCurrency());
        so.setBillStatus(0);
        so.setStatus(1);
        settlementOrderMapper.insert(so);

        SettlementOrderItem soItem = new SettlementOrderItem();
        soItem.setSettlementOrderId(so.getId());
        soItem.setOutAccountType("PLATFORM");
        soItem.setOutAccountId(0L);
        soItem.setInAccountType(AccountType.SHOP_BALANCE.getCode());
        soItem.setInAccountId(shopId);
        soItem.setAmount(so.getAmount());
        soItem.setFeeType("GOODS");
        soItem.setTradeType("PAYMENT");
        settlementOrderItemMapper.insert(soItem);

        autoPostToBill(so, shopId, merchantId);
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

        long amountCents = refund.getAmount() != null ? refund.getAmount() : 0L;
        SettlementOrder reversal = new SettlementOrder();
        reversal.setSettlementNo("SR" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        reversal.setDirection(SettlementDirection.REVERSE.getCode());
        reversal.setBizType("REFUND");
        reversal.setPaymentId(refund.getPaymentId());
        reversal.setOrderId(order.getId());
        reversal.setMerchantId(order.getMerchantId());
        reversal.setShopId(order.getShopId());
        reversal.setAmount(amountCents);
        reversal.setCurrency("CNY");
        reversal.setBillStatus(0);
        reversal.setStatus(1);
        reversal.setIdempotentKey(idempotentKey);
        settlementOrderMapper.insert(reversal);
        return reversal;
    }

    private void autoPostToBill(SettlementOrder so, Long shopId, Long merchantId) {
        SettlementBill bill = settlementBillMapper.selectOne(new LambdaQueryWrapper<SettlementBill>()
                .eq(SettlementBill::getShopId, shopId)
                .eq(SettlementBill::getSettleStatus, 0)
                .last("LIMIT 1"));

        if (bill == null) {
            bill = new SettlementBill();
            bill.setBillNo(UUID.randomUUID().toString().replace("-", ""));
            bill.setMerchantId(merchantId);
            bill.setShopId(shopId);
            bill.setPayeeType("SHOP");
            bill.setPayeeId(shopId);
            bill.setPeriodStart(System.currentTimeMillis());
            bill.setCurrency(so.getCurrency());
            bill.setBillAmount(0L);
            bill.setPreSettleAmount(0L);
            bill.setStatus("OPEN");
            bill.setSettleStatus(0);
            settlementBillMapper.insert(bill);
        }

        long billAmount = bill.getBillAmount() != null ? bill.getBillAmount() : 0L;
        long soAmount = so.getAmount() != null ? so.getAmount() : 0L;
        bill.setBillAmount(billAmount + soAmount);
        settlementBillMapper.updateById(bill);

        SettlementBillRef ref = new SettlementBillRef();
        ref.setSettlementBillId(bill.getId());
        ref.setSettlementOrderId(so.getId());
        settlementBillRefMapper.insert(ref);

        so.setBillStatus(1);
        settlementOrderMapper.updateById(so);
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

        long billAmount = bill.getBillAmount() != null ? bill.getBillAmount() : 0L;
        long soAmount = so.getAmount() != null ? so.getAmount() : 0L;
        bill.setBillAmount(billAmount + soAmount);
        settlementBillMapper.updateById(bill);

        so.setBillStatus(1);
        settlementOrderMapper.updateById(so);
    }

    @Transactional
    public SettlementVO settle(Long billId) {
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
                long amt = so.getAmount() != null ? so.getAmount() : 0L;
                netAmount += "FORWARD".equals(so.getDirection()) ? amt : -amt;
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

        return toSettlementVO(settlement);
    }

    /** Alias used by {@code SettlementController}. */
    @Transactional
    public void settleBill(Long billId) {
        settle(billId);
    }

    public Page<SettlementOrderVO> listOrders(int page, int size, Long shopId, Long merchantId) {
        Page<SettlementOrder> p = settlementOrderMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SettlementOrder>()
                        .eq(shopId != null, SettlementOrder::getShopId, shopId)
                        .eq(merchantId != null, SettlementOrder::getMerchantId, merchantId)
                        .orderByDesc(SettlementOrder::getCreatedAt));
        return mapOrderPage(p);
    }

    public Page<SettlementOrderVO> listOrders(int page, int size, Long merchantId) {
        return listOrders(page, size, null, merchantId);
    }

    public Page<SettlementBillVO> listBills(int page, int size, Long shopId, Long merchantId) {
        Page<SettlementBill> p = settlementBillMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SettlementBill>()
                        .eq(shopId != null, SettlementBill::getShopId, shopId)
                        .eq(merchantId != null, SettlementBill::getMerchantId, merchantId)
                        .orderByDesc(SettlementBill::getCreatedAt));
        return mapBillPage(p);
    }

    public Page<SettlementBillVO> listBills(int page, int size, Long merchantId) {
        return listBills(page, size, null, merchantId);
    }

    public Page<SettlementBillVO> listBillsForShop(int page, int size, Long shopId) {
        if (shopId == null) throw new BizException("店铺 ID 不能为空");
        return listBills(page, size, shopId, null);
    }

    public Page<SettlementOrderVO> listOrdersForShop(int page, int size, Long shopId) {
        if (shopId == null) throw new BizException("店铺 ID 不能为空");
        return listOrders(page, size, shopId, null);
    }

    @Transactional
    public SettlementBillVO createBill(Long shopId) {
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
        return toBillVO(bill);
    }

    private Page<SettlementBillVO> mapBillPage(Page<SettlementBill> p) {
        Page<SettlementBillVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toBillVO).toList());
        return result;
    }

    private Page<SettlementOrderVO> mapOrderPage(Page<SettlementOrder> p) {
        Page<SettlementOrderVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toOrderVO).toList());
        return result;
    }

    private SettlementBillVO toBillVO(SettlementBill b) {
        SettlementBillVO vo = new SettlementBillVO();
        vo.setId(b.getId());
        vo.setBillNo(b.getBillNo());
        vo.setMerchantId(b.getMerchantId());
        vo.setShopId(b.getShopId());
        vo.setPayeeType(b.getPayeeType());
        vo.setPayeeId(b.getPayeeId());
        vo.setPeriodStart(b.getPeriodStart());
        vo.setPeriodEnd(b.getPeriodEnd());
        vo.setCurrency(b.getCurrency());
        vo.setBillAmount(b.getBillAmount());
        vo.setPreSettleAmount(b.getPreSettleAmount());
        vo.setStatus(b.getStatus());
        vo.setSettleStatus(b.getSettleStatus());
        vo.setCreatedAt(b.getCreatedAt());
        return vo;
    }

    private SettlementOrderVO toOrderVO(SettlementOrder o) {
        SettlementOrderVO vo = new SettlementOrderVO();
        vo.setId(o.getId());
        vo.setSettlementNo(o.getSettlementNo());
        vo.setDirection(o.getDirection());
        vo.setBizType(o.getBizType());
        vo.setPaymentId(o.getPaymentId());
        vo.setOrderId(o.getOrderId());
        vo.setMerchantId(o.getMerchantId());
        vo.setShopId(o.getShopId());
        vo.setAmount(o.getAmount());
        vo.setCurrency(o.getCurrency());
        vo.setBillStatus(o.getBillStatus());
        vo.setStatus(o.getStatus());
        vo.setExtra(o.getExtra());
        vo.setCreatedAt(o.getCreatedAt());
        return vo;
    }

    private SettlementVO toSettlementVO(Settlement s) {
        SettlementVO vo = new SettlementVO();
        vo.setId(s.getId());
        vo.setSettleNo(s.getSettleNo());
        vo.setSettlementBillId(s.getSettlementBillId());
        vo.setMerchantId(s.getMerchantId());
        vo.setShopId(s.getShopId());
        vo.setNetAmount(s.getNetAmount());
        vo.setCurrency(s.getCurrency());
        vo.setStatus(s.getStatus());
        vo.setSettledAt(s.getSettledAt());
        vo.setCreatedAt(s.getCreatedAt());
        return vo;
    }
}
