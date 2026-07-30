package com.john.ecommerce.module.payment.service;

import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.payment.channel.PayChannel;
import com.john.ecommerce.module.payment.channel.RefundContext;
import com.john.ecommerce.module.payment.channel.RefundResult;
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
        if (payment.getChannelType() == null) throw new BizException("无渠道类型");

        PayChannelConfig config = payment.getChannelConfigId() != null
                ? channelConfigMapper.selectById(payment.getChannelConfigId())
                : null;
        PayChannel channel = channelRegistry.get(payment.getChannelType());
        if (channel == null) throw new BizException("渠道未实现");

        Payment refundPayment = new Payment();
        refundPayment.setAmount(refundAmount);
        refundPayment.setParentPaymentId(payment.getId());

        RefundContext ctx = new RefundContext();
        ctx.setPayment(payment);
        ctx.setRefundPayment(refundPayment);
        ctx.setConfig(config);

        RefundResult result = channel.refund(ctx);

        ChannelRefundResult out = new ChannelRefundResult();
        out.setSuccess(result.isSuccess());
        out.setRefundedAmount(refundAmount);
        out.setMessage(result.getChannelRefundNo());
        return out;
    }
}
