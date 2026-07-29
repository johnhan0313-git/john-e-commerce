package com.john.ecommerce.module.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.payment.dto.SettlementBillVO;
import com.john.ecommerce.module.payment.dto.SettlementOrderVO;
import com.john.ecommerce.module.payment.entity.*;
import com.john.ecommerce.module.payment.mapper.*;
import com.john.ecommerce.module.trade.entity.Order;
import com.john.ecommerce.module.trade.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementOrderMapper settlementOrderMapper;
    private final SettlementOrderItemMapper settlementOrderItemMapper;
    private final SettlementBillMapper settlementBillMapper;
    private final SettlementBillRefMapper settlementBillRefMapper;
    private final SettlementMapper settlementMapper;
    private final SettlementItemMapper settlementItemMapper;
    private final SettlementRefMapper settlementRefMapper;
    private final OrderMapper orderMapper;

    @Transactional
    public void createSettlementOrder(Payment payment, PaymentItem item, String direction) {
        Order order = orderMapper.selectById(item.getOrderId());
        Long merchantId = order != null ? order.getMerchantId() : null;

        SettlementOrder so = new SettlementOrder();
        so.setSettlementNo(UUID.randomUUID().toString().replace("-", ""));
        so.setDirection(direction);
        so.setBizType("PAYMENT");
        so.setPaymentId(payment.getId());
        so.setOrderId(item.getOrderId());
        so.setMerchantId(merchantId);
        so.setAmount(item.getAmount().movePointRight(2).longValue());
        so.setCurrency(payment.getCurrency());
        so.setBillStatus(0);
        so.setStatus(1);
        settlementOrderMapper.insert(so);

        SettlementOrderItem soItem = new SettlementOrderItem();
        soItem.setSettlementOrderId(so.getId());
        soItem.setOutAccountType("PLATFORM");
        soItem.setOutAccountId(0L);
        soItem.setInAccountType("MERCHANT_BALANCE");
        soItem.setInAccountId(merchantId != null ? merchantId : 0L);
        soItem.setAmount(so.getAmount());
        soItem.setFeeType("GOODS");
        soItem.setTradeType("PAYMENT");
        settlementOrderItemMapper.insert(soItem);

        postToBill(so, merchantId);
    }

    @Transactional
    public void postToBill(SettlementOrder so, Long merchantId) {
        SettlementBill bill = settlementBillMapper.selectOne(new LambdaQueryWrapper<SettlementBill>()
                .eq(SettlementBill::getMerchantId, merchantId)
                .eq(SettlementBill::getSettleStatus, 0)
                .last("LIMIT 1"));

        if (bill == null) {
            bill = new SettlementBill();
            bill.setBillNo(UUID.randomUUID().toString().replace("-", ""));
            bill.setMerchantId(merchantId);
            bill.setPayeeType("MERCHANT");
            bill.setPayeeId(merchantId);
            bill.setPeriodStart(System.currentTimeMillis());
            bill.setCurrency(so.getCurrency());
            bill.setBillAmount(0L);
            bill.setPreSettleAmount(0L);
            bill.setStatus("OPEN");
            bill.setSettleStatus(0);
            settlementBillMapper.insert(bill);
        }

        bill.setBillAmount(bill.getBillAmount() + so.getAmount());
        settlementBillMapper.updateById(bill);

        SettlementBillRef ref = new SettlementBillRef();
        ref.setSettlementBillId(bill.getId());
        ref.setSettlementOrderId(so.getId());
        settlementBillRefMapper.insert(ref);

        so.setBillStatus(1);
        settlementOrderMapper.updateById(so);
    }

    @Transactional
    public void settleBill(Long billId) {
        SettlementBill bill = settlementBillMapper.selectById(billId);
        if (bill == null) throw new BizException("账单不存在");
        if (bill.getSettleStatus() != 0) throw new BizException("账单已结算");

        List<SettlementBillRef> refs = settlementBillRefMapper.selectList(
                new LambdaQueryWrapper<SettlementBillRef>()
                        .eq(SettlementBillRef::getSettlementBillId, billId));

        List<Long> soIds = refs.stream().map(SettlementBillRef::getSettlementOrderId).toList();
        if (soIds.isEmpty()) throw new BizException("账单无结算单");

        List<SettlementOrderItem> allItems = new ArrayList<>();
        for (Long soId : soIds) {
            allItems.addAll(settlementOrderItemMapper.selectList(
                    new LambdaQueryWrapper<SettlementOrderItem>()
                            .eq(SettlementOrderItem::getSettlementOrderId, soId)));
        }

        // netting by account pairs
        Map<String, Long> netting = new HashMap<>();
        for (SettlementOrderItem item : allItems) {
            String key = item.getOutAccountType() + ":" + item.getOutAccountId()
                    + "->" + item.getInAccountType() + ":" + item.getInAccountId();
            netting.merge(key, item.getAmount(), Long::sum);
        }

        Settlement settlement = new Settlement();
        settlement.setSettleNo(UUID.randomUUID().toString().replace("-", ""));
        settlement.setSettlementBillId(bill.getId());
        settlement.setMerchantId(bill.getMerchantId());
        settlement.setNetAmount(bill.getBillAmount());
        settlement.setCurrency(bill.getCurrency());
        settlement.setStatus(1);
        settlement.setSettledAt(System.currentTimeMillis());
        settlementMapper.insert(settlement);

        for (Map.Entry<String, Long> entry : netting.entrySet()) {
            String[] parts = entry.getKey().split("->");
            String[] out = parts[0].split(":");
            String[] in = parts[1].split(":");

            SettlementItem si = new SettlementItem();
            si.setSettlementId(settlement.getId());
            si.setAccountType(in[0]);
            si.setAccountId(Long.parseLong(in[1]));
            si.setDirection("IN");
            si.setAmount(entry.getValue());
            settlementItemMapper.insert(si);
        }

        for (Long soId : soIds) {
            SettlementRef sRef = new SettlementRef();
            sRef.setSettlementId(settlement.getId());
            sRef.setSettlementOrderId(soId);
            settlementRefMapper.insert(sRef);
        }

        bill.setSettleStatus(1);
        bill.setStatus("SETTLED");
        bill.setPeriodEnd(System.currentTimeMillis());
        bill.setPreSettleAmount(bill.getBillAmount());
        settlementBillMapper.updateById(bill);
    }

    public Page<SettlementOrderVO> listOrders(int page, int size) {
        Page<SettlementOrder> p = settlementOrderMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SettlementOrder>().orderByDesc(SettlementOrder::getCreatedAt));
        Page<SettlementOrderVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toOrderVO).toList());
        return result;
    }

    public Page<SettlementBillVO> listBills(int page, int size) {
        Page<SettlementBill> p = settlementBillMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SettlementBill>().orderByDesc(SettlementBill::getCreatedAt));
        Page<SettlementBillVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toBillVO).toList());
        return result;
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
        vo.setAmount(o.getAmount());
        vo.setCurrency(o.getCurrency());
        vo.setBillStatus(o.getBillStatus());
        vo.setStatus(o.getStatus());
        vo.setExtra(o.getExtra());
        vo.setCreatedAt(o.getCreatedAt());
        return vo;
    }

    private SettlementBillVO toBillVO(SettlementBill b) {
        SettlementBillVO vo = new SettlementBillVO();
        vo.setId(b.getId());
        vo.setBillNo(b.getBillNo());
        vo.setMerchantId(b.getMerchantId());
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
}
