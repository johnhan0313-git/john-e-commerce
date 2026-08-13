package com.john.ecommerce.module.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.context.UserContext;
import com.john.ecommerce.common.enums.OrderStatus;
import com.john.ecommerce.common.enums.PayStatus;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.payment.channel.PayChannel;
import com.john.ecommerce.module.payment.channel.PaymentContext;
import com.john.ecommerce.module.payment.channel.PrepayResult;
import com.john.ecommerce.module.payment.dto.PaymentCreateDTO;
import com.john.ecommerce.module.payment.dto.PaymentVO;
import com.john.ecommerce.module.payment.entity.*;
import com.john.ecommerce.module.payment.enums.PaymentStatus;
import com.john.ecommerce.module.fulfillment.service.inventory.InventoryFacade;
import com.john.ecommerce.module.payment.mapper.PaymentItemMapper;
import com.john.ecommerce.module.payment.mapper.PaymentMapper;
import com.john.ecommerce.module.trade.entity.Order;
import com.john.ecommerce.module.trade.mapper.OrderMapper;
import com.john.ecommerce.module.user.identity.IdentityCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${app.payment.webhook-secret:}")
    private String webhookSecret;

    private final PaymentMapper paymentMapper;
    private final PaymentItemMapper paymentItemMapper;
    private final CashierRouter cashierRouter;
    private final List<PayChannel> payChannels;
    private final OrderMapper orderMapper;
    private final SettlementService settlementService;
    private final InventoryFacade inventoryFacade;

    @Transactional
    public PaymentVO createPayment(PaymentCreateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        Long userId = requireUserId();
        Set<Long> orderIds = new HashSet<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PaymentCreateDTO.Item item : dto.getItems()) {
            if (!orderIds.add(item.getOrderId())) {
                throw new BizException("支付项包含重复订单");
            }
            Order order = orderMapper.selectById(item.getOrderId());
            if (order == null || !userId.equals(order.getUserId())) {
                throw new BizException(403, "无权支付该订单");
            }
            if (order.getStatus() == null || order.getStatus() != OrderStatus.PENDING.getCode()
                    || order.getPayStatus() == null || order.getPayStatus() == PayStatus.PAID.getCode()) {
                throw new BizException("订单当前不可支付");
            }
            if (order.getPayDeadline() != null && order.getPayDeadline() <= System.currentTimeMillis()) {
                throw new BizException("订单已超过支付期限");
            }
            BigDecimal paid = order.getPaidAmount() != null ? order.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal remaining = order.getPayAmount().subtract(paid);
            if (remaining.signum() <= 0 || item.getAmount().compareTo(remaining) != 0) {
                throw new BizException("支付金额与订单剩余应付金额不一致");
            }
            totalAmount = totalAmount.add(remaining);
        }

        PayChannelConfig config = cashierRouter.route(tenantId, dto.getMethodCode(), null, totalAmount);

        Payment payment = new Payment();
        payment.setPayNo(UUID.randomUUID().toString().replace("-", ""));
        payment.setMethodCode(dto.getMethodCode());
        payment.setPayAccountId(config.getPayAccountId());
        payment.setChannelConfigId(config.getId());
        payment.setChannelType(config.getChannelType());
        payment.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "CNY");
        payment.setAmount(totalAmount);
        payment.setStatus(PaymentStatus.PENDING.getCode());
        paymentMapper.insert(payment);

        for (PaymentCreateDTO.Item item : dto.getItems()) {
            PaymentItem pi = new PaymentItem();
            pi.setPaymentId(payment.getId());
            pi.setOrderId(item.getOrderId());
            pi.setAmount(item.getAmount());
            paymentItemMapper.insert(pi);
        }

        PayChannel channel = resolveChannel(config.getChannelType());
        PaymentContext ctx = new PaymentContext();
        ctx.setPayment(payment);
        ctx.setConfig(config);
        PrepayResult result = channel.prepay(ctx);

        if (result.isSuccess()) {
            payment.setStatus(PaymentStatus.PROCESSING.getCode());
            payment.setChannelTradeNo(result.getChannelTradeNo());
        } else {
            payment.setStatus(PaymentStatus.FAILED.getCode());
        }
        paymentMapper.updateById(payment);

        return toVO(payment);
    }

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

        completePayment(payment);
    }

    private void completePayment(Payment payment) {
        long paidAt = System.currentTimeMillis();
        int claimed = paymentMapper.update(null, new LambdaUpdateWrapper<Payment>()
                .eq(Payment::getId, payment.getId())
                .in(Payment::getStatus, PaymentStatus.PENDING.getCode(), PaymentStatus.PROCESSING.getCode())
                .set(Payment::getStatus, PaymentStatus.SUCCESS.getCode())
                .set(Payment::getPaidAt, paidAt));
        if (claimed == 0) return;
        payment.setStatus(PaymentStatus.SUCCESS.getCode());
        payment.setPaidAt(paidAt);

        List<PaymentItem> items = paymentItemMapper.selectList(new LambdaQueryWrapper<PaymentItem>()
                .eq(PaymentItem::getPaymentId, payment.getId()));

        for (PaymentItem item : items) {
            Order order = orderMapper.selectById(item.getOrderId());
            if (order != null) {
                BigDecimal newPaid = (order.getPaidAmount() != null ? order.getPaidAmount() : BigDecimal.ZERO)
                        .add(item.getAmount());
                order.setPaidAmount(newPaid);
                boolean fullyPaid = newPaid.compareTo(order.getPayAmount()) >= 0;
                order.setPayStatus(fullyPaid ? PayStatus.PAID.getCode() : PayStatus.PARTIAL.getCode());
                order.setPayNo(payment.getPayNo());
                order.setPayTime(payment.getPaidAt());
                // 付清且仍为待支付：推进已支付并扣减锁定库存；已取消等状态不再 consume
                boolean shouldConsume = fullyPaid && order.getStatus() != null
                        && order.getStatus() == OrderStatus.PENDING.getCode();
                if (shouldConsume) {
                    order.setStatus(OrderStatus.PAID.getCode());
                } else if (fullyPaid && order.getStatus() != null
                        && order.getStatus() != OrderStatus.PENDING.getCode()) {
                    log.warn("支付成功但订单非待支付，跳过库存扣减 orderId={} status={}",
                            order.getId(), order.getStatus());
                }
                orderMapper.updateById(order);
                if (shouldConsume) {
                    inventoryFacade.consumeForOrder(order);
                }
            }

            settlementService.createSettlementOrder(payment, item, "FORWARD");
        }
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
        // Channel callbacks share the same idempotent settlement path as the mock channel.
        completePayment(payment);
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

    private PayChannel resolveChannel(String channelType) {
        return payChannels.stream()
                .filter(c -> c.supports(channelType))
                .findFirst()
                .orElseThrow(() -> new BizException("不支持的渠道: " + channelType));
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
