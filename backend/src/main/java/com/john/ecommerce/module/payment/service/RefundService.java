package com.john.ecommerce.module.payment.service;

import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.payment.channel.PayChannel;
import com.john.ecommerce.module.payment.channel.dto.ChannelRefundResult;
import com.john.ecommerce.module.payment.channel.route.PayChannelRegistry;
import com.john.ecommerce.module.payment.entity.PayChannelConfig;
import com.john.ecommerce.module.payment.entity.Payment;
import com.john.ecommerce.module.payment.mapper.PayChannelConfigMapper;
import com.john.ecommerce.module.payment.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final PaymentMapper paymentMapper;
    private final PayChannelConfigMapper channelConfigMapper;
    private final PayChannelRegistry channelRegistry;

    @Transactional
    public ChannelRefundResult refund(Long paymentId, BigDecimal refundAmount) {
        Payment payment = paymentMapper.selectById(paymentId);
        if (payment == null) throw new BizException("支付单不存在");
        if (payment.getChannelConfigId() == null) throw new BizException("无渠道配置");

        PayChannelConfig config = channelConfigMapper.selectById(payment.getChannelConfigId());
        PayChannel channel = channelRegistry.get(payment.getChannelType());
        if (channel == null) throw new BizException("渠道未实现");

        return channel.refund(config, payment.getChannelTradeNo(), refundAmount);
    }
}
