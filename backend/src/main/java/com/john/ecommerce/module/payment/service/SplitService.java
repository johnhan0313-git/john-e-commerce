package com.john.ecommerce.module.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.payment.channel.SplitChannel;
import com.john.ecommerce.module.payment.entity.SplitDetail;
import com.john.ecommerce.module.payment.entity.SplitOrder;
import com.john.ecommerce.module.payment.mapper.SplitDetailMapper;
import com.john.ecommerce.module.payment.mapper.SplitOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SplitService {

    private final SplitOrderMapper splitOrderMapper;
    private final SplitDetailMapper splitDetailMapper;
    private final List<SplitChannel> splitChannels;

    @Transactional
    public SplitOrder createSplit(Long paymentId, List<SplitDetail> details) {
        if (details == null || details.isEmpty()) {
            throw new BizException("分账明细不能为空");
        }
        long total = details.stream().mapToLong(d -> d.getAmount() != null ? d.getAmount() : 0L).sum();
        SplitOrder order = new SplitOrder();
        order.setPaymentId(paymentId);
        order.setSplitNo("SP" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        order.setChannelType("MOCK");
        order.setTotalAmount(total);
        order.setStatus(0);
        return createSplit(order, details);
    }

    @Transactional
    public SplitOrder createSplit(SplitOrder order, List<SplitDetail> details) {
        splitOrderMapper.insert(order);
        for (SplitDetail d : details) {
            d.setSplitOrderId(order.getId());
            splitDetailMapper.insert(d);
        }

        SplitChannel channel = resolveChannel(order.getChannelType());
        String channelSplitNo = channel.createSplit(order, details);
        order.setChannelSplitNo(channelSplitNo);
        order.setStatus(1);
        splitOrderMapper.updateById(order);
        return order;
    }

    @Transactional
    public void confirmSplit(Long splitOrderId) {
        SplitOrder order = splitOrderMapper.selectById(splitOrderId);
        if (order == null) throw new BizException("分账单不存在");

        SplitChannel channel = resolveChannel(order.getChannelType());
        boolean ok = channel.confirmSplit(order.getChannelSplitNo());
        if (!ok) throw new BizException("分账确认失败");

        order.setStatus(2);
        order.setConfirmedAt(System.currentTimeMillis());
        splitOrderMapper.updateById(order);
    }

    public List<SplitOrder> listByPayment(Long paymentId) {
        return splitOrderMapper.selectList(new LambdaQueryWrapper<SplitOrder>()
                .eq(SplitOrder::getPaymentId, paymentId)
                .orderByDesc(SplitOrder::getCreatedAt));
    }

    private SplitChannel resolveChannel(String channelType) {
        return splitChannels.stream()
                .filter(c -> c.supports(channelType))
                .findFirst()
                .orElseThrow(() -> new BizException("不支持的分账渠道: " + channelType));
    }
}
