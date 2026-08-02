package com.john.ecommerce.module.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.context.TenantContext;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

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
        BigDecimal totalAmount = dto.getItems().stream()
                .map(PaymentCreateDTO.Item::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

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
        if (payment.getStatus() == PaymentStatus.SUCCESS.getCode()) throw new BizException("已支付");

        payment.setStatus(PaymentStatus.SUCCESS.getCode());
        payment.setPaidAt(System.currentTimeMillis());
        paymentMapper.updateById(payment);

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

    public PaymentVO getById(Long id) {
        Payment payment = paymentMapper.selectById(id);
        if (payment == null) throw new BizException("支付单不存在");
        return toVO(payment);
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
