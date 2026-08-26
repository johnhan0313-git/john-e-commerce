package com.john.ecommerce.module.payment.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.john.ecommerce.module.payment.entity.PayChannelConfig;
import com.john.ecommerce.module.payment.entity.Payment;
import com.john.ecommerce.module.payment.entity.PaymentItem;
import com.john.ecommerce.module.payment.enums.PaymentStatus;
import com.john.ecommerce.module.payment.mapper.PaymentItemMapper;
import com.john.ecommerce.module.payment.mapper.PaymentMapper;
import com.john.ecommerce.module.payment.service.CashierRouter;
import com.john.ecommerce.module.trade.entity.Order;
import com.john.ecommerce.module.trade.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreatePaymentApplication {

    private final PaymentMapper paymentMapper;
    private final PaymentItemMapper paymentItemMapper;
    private final CashierRouter cashierRouter;
    private final List<PayChannel> payChannels;
    private final OrderMapper orderMapper;

    @Transactional
    public PaymentVO create(PaymentCreateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        Long userId = requireUserId();
        Set<Long> orderIds = new HashSet<>();
        long totalAmount = 0L;
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
            long paid = order.getPaidAmount() != null ? order.getPaidAmount() : 0L;
            long payAmount = order.getPayAmount() != null ? order.getPayAmount() : 0L;
            long remaining = payAmount - paid;
            long itemAmount = item.getAmount() != null ? item.getAmount() : 0L;
            if (remaining <= 0 || itemAmount != remaining) {
                throw new BizException("支付金额与订单剩余应付金额不一致");
            }
            totalAmount += remaining;
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
