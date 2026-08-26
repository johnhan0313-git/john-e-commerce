package com.john.ecommerce.module.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.context.UserContext;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.payment.application.CompletePaymentApplication;
import com.john.ecommerce.module.payment.dto.PaymentVO;
import com.john.ecommerce.module.payment.entity.Payment;
import com.john.ecommerce.module.payment.entity.PaymentItem;
import com.john.ecommerce.module.payment.enums.PaymentStatus;
import com.john.ecommerce.module.payment.mapper.PaymentItemMapper;
import com.john.ecommerce.module.payment.mapper.PaymentMapper;
import com.john.ecommerce.module.trade.entity.Order;
import com.john.ecommerce.module.trade.mapper.OrderMapper;
import com.john.ecommerce.module.user.identity.IdentityCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${app.payment.webhook-secret:}")
    private String webhookSecret;

    private final PaymentMapper paymentMapper;
    private final PaymentItemMapper paymentItemMapper;
    private final OrderMapper orderMapper;
    private final CompletePaymentApplication completePaymentApplication;

    @Transactional
    public void mockCallback(String payNo) {
        Payment payment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getPayNo, payNo));
        if (payment == null) throw new BizException("支付单不存在");
        if (!"MOCK".equalsIgnoreCase(payment.getChannelType())) {
            throw new BizException(403, "仅模拟支付支持此回调");
        }
        requirePaymentOwner(payment.getId());
        if (payment.getStatus() == PaymentStatus.SUCCESS.getCode()) return;
        if (payment.getStatus() != PaymentStatus.PENDING.getCode()
                && payment.getStatus() != PaymentStatus.PROCESSING.getCode()) {
            throw new BizException("支付单当前状态不可完成");
        }

        completePaymentApplication.complete(payment);
    }

    private String hmacSha256(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            StringBuilder out = new StringBuilder();
            for (byte b : mac.doFinal(payload.getBytes(StandardCharsets.UTF_8))) {
                out.append(String.format("%02x", b));
            }
            return out.toString();
        } catch (Exception e) {
            throw new BizException(503, "支付回调签名服务不可用");
        }
    }

    @Transactional
    public void channelCallback(String payNo, String timestamp, String signature) {
        if (payNo == null || timestamp == null || signature == null) {
            throw new BizException(400, "支付回调参数不完整");
        }
        String secret = webhookSecret;
        if (secret == null || secret.isBlank()) {
            throw new BizException(503, "支付回调密钥未配置");
        }
        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new BizException(400, "支付回调时间戳无效");
        }
        if (Math.abs(System.currentTimeMillis() - ts) > 300_000L) {
            throw new BizException(403, "支付回调已过期");
        }
        String expected = hmacSha256(payNo + "." + timestamp, secret);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                signature.trim().toLowerCase().getBytes(StandardCharsets.UTF_8))) {
            throw new BizException(403, "支付回调签名无效");
        }
        Payment payment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getPayNo, payNo));
        if (payment == null) throw new BizException("支付单不存在");
        if (payment.getStatus() == PaymentStatus.SUCCESS.getCode()) return;
        if (payment.getStatus() != PaymentStatus.PROCESSING.getCode()
                && payment.getStatus() != PaymentStatus.PENDING.getCode()) {
            throw new BizException("支付单当前状态不可完成");
        }
        completePaymentApplication.complete(payment);
    }

    public PaymentVO getById(Long id) {
        Payment payment = paymentMapper.selectById(id);
        if (payment == null) throw new BizException("支付单不存在");
        if (!UserContext.hasIdentity(IdentityCodes.OPS)) {
            requirePaymentOwner(payment.getId());
        }
        return toVO(payment);
    }

    private void requirePaymentOwner(Long paymentId) {
        Long userId = requireUserId();
        List<PaymentItem> items = paymentItemMapper.selectList(new LambdaQueryWrapper<PaymentItem>()
                .eq(PaymentItem::getPaymentId, paymentId));
        if (items.isEmpty()) throw new BizException("支付单没有支付项");
        for (PaymentItem item : items) {
            Order order = orderMapper.selectById(item.getOrderId());
            if (order == null || !userId.equals(order.getUserId())) {
                throw new BizException(403, "无权访问该支付单");
            }
        }
    }

    private Long requireUserId() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw new BizException(401, "用户未登录");
        return userId;
    }

    private PaymentVO toVO(Payment p) {
        PaymentVO vo = new PaymentVO();
        vo.setId(p.getId());
        vo.setPayNo(p.getPayNo());
        vo.setMethodCode(p.getMethodCode());
        vo.setChannelType(p.getChannelType());
        vo.setCurrency(p.getCurrency());
        vo.setAmount(p.getAmount());
        vo.setStatus(p.getStatus());
        vo.setChannelTradeNo(p.getChannelTradeNo());
        vo.setPaidAt(p.getPaidAt());
        vo.setExtra(p.getExtra());
        vo.setCreatedAt(p.getCreatedAt());

        List<PaymentItem> items = paymentItemMapper.selectList(new LambdaQueryWrapper<PaymentItem>()
                .eq(PaymentItem::getPaymentId, p.getId()));
        vo.setItems(items.stream().map(i -> {
            PaymentVO.PaymentItemVO iv = new PaymentVO.PaymentItemVO();
            iv.setId(i.getId());
            iv.setOrderId(i.getOrderId());
            iv.setAmount(i.getAmount());
            return iv;
        }).toList());
        return vo;
    }
}
