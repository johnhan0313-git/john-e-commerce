package com.john.ecommerce.module.payment.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.context.UserContext;
import com.john.ecommerce.common.enums.OrderStatus;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.common.outbox.OutboxEventTypes;
import com.john.ecommerce.common.outbox.OutboxPort;
import com.john.ecommerce.module.payment.channel.PayChannel;
import com.john.ecommerce.module.payment.channel.RefundContext;
import com.john.ecommerce.module.payment.channel.RefundResult;
import com.john.ecommerce.module.payment.channel.dto.ChannelRefundResult;
import com.john.ecommerce.module.payment.channel.route.PayChannelRegistry;
import com.john.ecommerce.module.payment.dto.RefundVO;
import com.john.ecommerce.module.payment.entity.PayChannelConfig;
import com.john.ecommerce.module.payment.entity.Payment;
import com.john.ecommerce.module.payment.mapper.PayChannelConfigMapper;
import com.john.ecommerce.module.payment.mapper.PaymentMapper;
import com.john.ecommerce.module.payment.service.SettlementBillService;
import com.john.ecommerce.module.trade.dto.RefundApplyDTO;
import com.john.ecommerce.module.trade.entity.Order;
import com.john.ecommerce.module.trade.entity.OrderItem;
import com.john.ecommerce.module.trade.entity.Refund;
import com.john.ecommerce.module.trade.entity.RefundItem;
import com.john.ecommerce.module.trade.mapper.OrderItemMapper;
import com.john.ecommerce.module.trade.mapper.OrderMapper;
import com.john.ecommerce.module.trade.mapper.RefundItemMapper;
import com.john.ecommerce.module.trade.mapper.RefundMapper;
import com.john.ecommerce.module.trade.port.OrderLifecyclePort;
import com.john.ecommerce.module.trade.port.command.CompleteRefundCommand;
import com.john.ecommerce.module.trade.port.command.RestoreAfterRefundCommand;
import com.john.ecommerce.module.trade.port.command.StartRefundCommand;
import com.john.ecommerce.module.user.identity.IdentityCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefundApplication {

    public static final int PENDING = 0;
    public static final int PROCESSING = 1;
    public static final int COMPLETED = 2;
    public static final int REJECTED = 3;
    public static final int FAILED = 4;

    @Value("${app.payment.webhook-secret:}")
    private String webhookSecret;

    private final PaymentMapper paymentMapper;
    private final PayChannelConfigMapper channelConfigMapper;
    private final PayChannelRegistry channelRegistry;
    private final RefundMapper refundMapper;
    private final OrderMapper orderMapper;
    private final OrderLifecyclePort orderLifecyclePort;
    private final SettlementBillService settlementBillService;
    private final RefundItemMapper refundItemMapper;
    private final OrderItemMapper orderItemMapper;
    private final OutboxPort outboxPort;

    @Transactional
    public RefundVO apply(Long orderId, RefundApplyDTO dto) {
        Long userId = requireUser();
        Order order = orderMapper.selectById(orderId);
        if (order == null || !userId.equals(order.getUserId())) throw new BizException(403, "无权申请该订单退款");
        if (order.getStatus() == null || order.getStatus() < OrderStatus.PAID.getCode()
                || order.getStatus() == OrderStatus.CANCELLED.getCode()
                || order.getStatus() == OrderStatus.REFUNDED.getCode()) {
            throw new BizException("订单当前不可退款");
        }
        long paid = order.getPaidAmount() != null ? order.getPaidAmount() : 0L;
        long existing = refundMapper.selectList(new LambdaQueryWrapper<Refund>()
                        .eq(Refund::getOrderId, orderId).in(Refund::getStatus, 0, 1, 2))
                .stream().map(Refund::getAmount).map(a -> a != null ? a : 0L).reduce(0L, Long::sum);
        long requestAmount = dto.getAmount() != null ? dto.getAmount() : 0L;
        if (requestAmount > paid - existing) throw new BizException("退款金额超过可退金额");
        Refund refund = new Refund();
        refund.setRefundNo("RF" + UUID.randomUUID().toString().replace("-", "").substring(0, 24));
        refund.setOrderId(orderId);
        refund.setPaymentId(order.getPayNo() == null ? null : findPaymentId(order.getPayNo()));
        refund.setUserId(userId);
        refund.setAmount(dto.getAmount());
        refund.setReason(dto.getReason());
        refund.setStatus(0);
        refundMapper.insert(refund);
        createRefundItems(refund, order, dto);
        orderLifecyclePort.startRefund(StartRefundCommand.builder().orderId(orderId).build());
        return toVO(refund);
    }

    @Transactional
    public RefundVO approve(Long refundId, boolean approved) {
        if (!UserContext.hasIdentity(IdentityCodes.OPS)) throw new BizException(403, "无权审核退款");
        Refund refund = refundMapper.selectById(refundId);
        if (refund == null || refund.getStatus() != PENDING) throw new BizException("退款申请当前不可审核");
        if (!approved) {
            refund.setStatus(REJECTED);
            refundMapper.updateById(refund);
            orderLifecyclePort.restoreAfterRefund(RestoreAfterRefundCommand.builder()
                    .orderId(refund.getOrderId()).build());
            return toVO(refund);
        }
        refund.setStatus(PROCESSING);
        refundMapper.updateById(refund);
        ChannelRefundResult result = channelRefund(refund.getPaymentId(), refund.getAmount());
        if (!result.isSuccess()) throw new BizException("渠道退款失败");
        refund.setStatus(COMPLETED);
        refund.setRefundedAt(System.currentTimeMillis());
        refund.setChannelRefundNo(result.getMessage());
        refundMapper.updateById(refund);
        Order order = orderMapper.selectById(refund.getOrderId());
        if (order != null) settlementBillService.createRefundReversal(refund, order);
        applyOrderStatusAfterRefundSuccess(refund, order);
        return toVO(refund);
    }

    @Transactional
    public RefundVO callback(String refundNo, String timestamp, String signature, boolean success, String channelRefundNo) {
        verifySignature(refundNo, timestamp, signature, success);
        Refund refund = refundMapper.selectOne(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getRefundNo, refundNo));
        if (refund == null) throw new BizException("退款申请不存在");
        if (refund.getStatus() == COMPLETED || refund.getStatus() == REJECTED || refund.getStatus() == FAILED) {
            return toVO(refund);
        }
        if (refund.getStatus() != PROCESSING) throw new BizException("退款申请当前不可回调");
        if (success) {
            refund.setStatus(COMPLETED);
            refund.setRefundedAt(System.currentTimeMillis());
            refund.setChannelRefundNo(channelRefundNo);
            refundMapper.updateById(refund);
            Order order = orderMapper.selectById(refund.getOrderId());
            if (order != null) settlementBillService.createRefundReversal(refund, order);
            applyOrderStatusAfterRefundSuccess(refund, order);
        } else {
            refund.setStatus(FAILED);
            refundMapper.updateById(refund);
            orderLifecyclePort.restoreAfterRefund(RestoreAfterRefundCommand.builder()
                    .orderId(refund.getOrderId()).build());
        }
        return toVO(refund);
    }

    public RefundVO get(Long id) {
        Refund refund = refundMapper.selectById(id);
        if (refund == null) throw new BizException("退款申请不存在");
        if (!UserContext.hasIdentity(IdentityCodes.OPS) && !requireUser().equals(refund.getUserId())) {
            throw new BizException(403, "无权访问该退款申请");
        }
        return toVO(refund);
    }

    public Page<RefundVO> list(int page, int size) {
        var wrapper = new LambdaQueryWrapper<Refund>();
        if (!UserContext.hasIdentity(IdentityCodes.OPS)) wrapper.eq(Refund::getUserId, requireUser());
        Page<Refund> p = refundMapper.selectPage(new Page<>(page, size), wrapper.orderByDesc(Refund::getCreatedAt));
        Page<RefundVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    private ChannelRefundResult channelRefund(Long paymentId, Long refundAmount) {
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

    private void applyOrderStatusAfterRefundSuccess(Refund refund, Order order) {
        if (order == null) return;
        long paid = order.getPaidAmount() != null ? order.getPaidAmount() : 0L;
        long refundAmount = refund.getAmount() != null ? refund.getAmount() : 0L;
        boolean full = refundAmount >= paid;
        Integer before = readStatusBeforeRefund(order);
        if (before != null && before == OrderStatus.PAID.getCode()) {
            restoreStockIfUnshipped(refund, order);
        }
        if (full) {
            orderLifecyclePort.completeRefund(CompleteRefundCommand.builder().orderId(order.getId()).build());
        } else {
            orderLifecyclePort.restoreAfterRefund(RestoreAfterRefundCommand.builder().orderId(order.getId()).build());
        }
    }

    private Integer readStatusBeforeRefund(Order order) {
        if (order.getExtra() == null) return null;
        Object v = order.getExtra().get(
                com.john.ecommerce.module.trade.application.OrderLifecycleApplication.EXTRA_STATUS_BEFORE_REFUND);
        if (v instanceof Number n) return n.intValue();
        return null;
    }

    private void verifySignature(String refundNo, String timestamp, String signature, boolean success) {
        if (webhookSecret == null || webhookSecret.isBlank()) throw new BizException(503, "支付回调密钥未配置");
        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (Exception e) {
            throw new BizException(400, "回调时间戳无效");
        }
        if (Math.abs(System.currentTimeMillis() - ts) > 300_000L) throw new BizException(403, "回调已过期");
        String payload = refundNo + "." + (success ? "1" : "0") + "." + timestamp;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            StringBuilder hex = new StringBuilder();
            for (byte b : mac.doFinal(payload.getBytes(StandardCharsets.UTF_8))) {
                hex.append(String.format("%02x", b));
            }
            if (!MessageDigest.isEqual(hex.toString().getBytes(StandardCharsets.UTF_8),
                    signature.trim().toLowerCase().getBytes(StandardCharsets.UTF_8))) {
                throw new BizException(403, "回调签名无效");
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(503, "回调签名服务不可用");
        }
    }

    private Long requireUser() {
        Long id = UserContext.getCurrentUserId();
        if (id == null) throw new BizException(401, "用户未登录");
        return id;
    }

    private Long findPaymentId(String payNo) {
        Payment p = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>().eq(Payment::getPayNo, payNo));
        return p == null ? null : p.getId();
    }

    private void createRefundItems(Refund refund, Order order, RefundApplyDTO dto) {
        Set<Long> seen = new HashSet<>();
        long calculated = 0L;
        for (RefundApplyDTO.Item requested : dto.getItems()) {
            if (!seen.add(requested.getOrderItemId())) throw new BizException("退款商品重复");
            OrderItem orderItem = orderItemMapper.selectById(requested.getOrderItemId());
            if (orderItem == null || !order.getId().equals(orderItem.getOrderId())) {
                throw new BizException("退款商品不属于该订单");
            }
            List<Long> activeRefundIds = refundMapper.selectList(
                            new LambdaQueryWrapper<Refund>()
                                    .eq(Refund::getOrderId, order.getId())
                                    .in(Refund::getStatus, PENDING, PROCESSING, COMPLETED))
                    .stream().map(Refund::getId).toList();
            int already = activeRefundIds.isEmpty() ? 0 : refundItemMapper.selectList(
                            new LambdaQueryWrapper<RefundItem>()
                                    .eq(RefundItem::getOrderItemId, orderItem.getId())
                                    .in(RefundItem::getRefundId, activeRefundIds))
                    .stream().mapToInt(i -> i.getQuantity() != null ? i.getQuantity() : 0).sum();
            if (already + requested.getQuantity() > orderItem.getQuantity()) {
                throw new BizException("退款数量超过购买数量");
            }
            long payAmount = orderItem.getPayAmount() != null ? orderItem.getPayAmount() : 0L;
            int orderQty = orderItem.getQuantity() != null ? orderItem.getQuantity() : 0;
            if (orderQty <= 0) throw new BizException("订单项数量无效");
            long itemAmount = (payAmount * requested.getQuantity()) / orderQty;
            RefundItem item = new RefundItem();
            item.setRefundId(refund.getId());
            item.setOrderItemId(orderItem.getId());
            item.setSkuId(orderItem.getSkuId());
            item.setQuantity(requested.getQuantity());
            item.setAmount(itemAmount);
            item.setStockRestored(0);
            refundItemMapper.insert(item);
            calculated += itemAmount;
        }
        long requestAmount = dto.getAmount() != null ? dto.getAmount() : 0L;
        if (calculated != requestAmount) throw new BizException("退款金额与商品明细不一致");
    }

    private void restoreStockIfUnshipped(Refund refund, Order order) {
        if (order == null || order.getWarehouseId() == null) return;
        List<RefundItem> items = refundItemMapper.selectList(new LambdaQueryWrapper<RefundItem>()
                .eq(RefundItem::getRefundId, refund.getId()));
        List<Map<String, Object>> lines = new ArrayList<>();
        for (RefundItem item : items) {
            if (item.getStockRestored() != null && item.getStockRestored() == 1) continue;
            int qty = item.getQuantity() != null ? item.getQuantity() : 0;
            if (qty <= 0 || item.getSkuId() == null) continue;
            Map<String, Object> line = new HashMap<>();
            line.put("refundItemId", item.getId());
            line.put("skuId", item.getSkuId());
            line.put("quantity", qty);
            lines.add(line);
        }
        if (lines.isEmpty()) return;
        Map<String, Object> payload = new HashMap<>();
        payload.put("refundId", refund.getId());
        payload.put("orderId", order.getId());
        payload.put("warehouseId", order.getWarehouseId());
        payload.put("lines", lines);
        Long tenantId = order.getTenantId() != null ? order.getTenantId() : refund.getTenantId();
        outboxPort.append(
                tenantId,
                OutboxEventTypes.REFUND_COMPLETED,
                "Refund",
                refund.getId(),
                payload,
                "RefundCompleted:" + refund.getId());
    }

    private RefundVO toVO(Refund r) {
        RefundVO vo = new RefundVO();
        vo.setId(r.getId());
        vo.setRefundNo(r.getRefundNo());
        vo.setOrderId(r.getOrderId());
        vo.setPaymentId(r.getPaymentId());
        vo.setUserId(r.getUserId());
        vo.setAmount(r.getAmount());
        vo.setReason(r.getReason());
        vo.setStatus(r.getStatus());
        vo.setRefundedAt(r.getRefundedAt());
        vo.setChannelRefundNo(r.getChannelRefundNo());
        vo.setCreatedAt(r.getCreatedAt());
        return vo;
    }
}
