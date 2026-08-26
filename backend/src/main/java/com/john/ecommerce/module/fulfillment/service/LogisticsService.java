package com.john.ecommerce.module.fulfillment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.fulfillment.dto.LogisticsCreateDTO;
import com.john.ecommerce.module.fulfillment.dto.LogisticsVO;
import com.john.ecommerce.module.fulfillment.dto.LogisticsWebhookDTO;
import com.john.ecommerce.module.fulfillment.entity.LogisticsItem;
import com.john.ecommerce.module.fulfillment.entity.LogisticsOrder;
import com.john.ecommerce.module.fulfillment.mapper.LogisticsItemMapper;
import com.john.ecommerce.module.fulfillment.mapper.LogisticsOrderMapper;
import com.john.ecommerce.module.trade.entity.Order;
import com.john.ecommerce.module.trade.entity.OrderItem;
import com.john.ecommerce.module.trade.mapper.OrderItemMapper;
import com.john.ecommerce.module.trade.mapper.OrderMapper;
import com.john.ecommerce.module.trade.port.OrderLifecyclePort;
import com.john.ecommerce.module.trade.port.command.CompleteCommand;
import com.john.ecommerce.module.trade.port.command.DeliverCommand;
import com.john.ecommerce.module.trade.port.command.ShipCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogisticsService {

    @Value("${app.logistics.webhook-secret:}")
    private String webhookSecret;

    private final LogisticsOrderMapper logisticsOrderMapper;
    private final LogisticsItemMapper logisticsItemMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderLifecyclePort orderLifecyclePort;

    @Transactional
    public LogisticsVO createShipment(LogisticsCreateDTO dto) {
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) throw new BizException("订单不存在");

        LogisticsOrder lo = new LogisticsOrder();
        lo.setOrderId(dto.getOrderId());
        lo.setLogisticsNo("LG" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000)));
        lo.setProvider(dto.getProvider());
        lo.setTrackingNo(dto.getTrackingNo());
        lo.setStatus(0);
        logisticsOrderMapper.insert(lo);

        for (LogisticsCreateDTO.Item item : dto.getItems()) {
            LogisticsItem li = new LogisticsItem();
            li.setLogisticsId(lo.getId());
            li.setOrderId(dto.getOrderId());
            li.setOrderItemId(item.getOrderItemId());
            li.setQty(item.getQty());
            logisticsItemMapper.insert(li);
        }

        updateOrderShipStatus(order);
        return toVO(lo);
    }

    @Transactional
    public void webhook(String trackingNo, LogisticsWebhookDTO dto, String timestamp, String signature) {
        verifyWebhook(trackingNo, dto, timestamp, signature);
        LogisticsOrder lo = logisticsOrderMapper.selectByTrackingNoIgnoreTenant(trackingNo);
        if (lo == null) throw new BizException("物流单不存在: " + trackingNo);

        Long prevTenant = TenantContext.getTenantId();
        if (lo.getTenantId() != null) {
            TenantContext.setTenantId(lo.getTenantId());
        }
        try {
            if (dto.getStatus() != null && dto.getStatus() == 1) {
                lo.setStatus(2);
                lo.setDeliveredAt(dto.getEventTime() != null ? dto.getEventTime() : System.currentTimeMillis());
                logisticsOrderMapper.updateById(lo);

                if (lo.getOrderId() != null) {
                    orderLifecyclePort.markDelivered(DeliverCommand.builder().orderId(lo.getOrderId()).build());
                }
            } else {
                lo.setStatus(1);
                lo.setShippedAt(dto.getEventTime() != null ? dto.getEventTime() : System.currentTimeMillis());
                logisticsOrderMapper.updateById(lo);
            }
        } finally {
            if (prevTenant == null) {
                TenantContext.clear();
            } else {
                TenantContext.setTenantId(prevTenant);
            }
        }
    }

    private void verifyWebhook(String trackingNo, LogisticsWebhookDTO dto, String timestamp, String signature) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new BizException(503, "物流回调密钥未配置");
        }
        if (timestamp == null || signature == null || dto == null || dto.getStatus() == null) {
            throw new BizException(400, "物流回调参数不完整");
        }
        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new BizException(400, "物流回调时间戳无效");
        }
        if (Math.abs(System.currentTimeMillis() - ts) > 300_000L) {
            throw new BizException(403, "物流回调已过期");
        }
        String payload = trackingNo + "." + dto.getStatus() + "."
                + (dto.getEventTime() != null ? dto.getEventTime() : "") + "." + timestamp;
        String expected;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            StringBuilder hex = new StringBuilder();
            for (byte b : mac.doFinal(payload.getBytes(StandardCharsets.UTF_8))) {
                hex.append(String.format("%02x", b));
            }
            expected = hex.toString();
        } catch (Exception e) {
            throw new BizException(503, "物流回调签名服务不可用");
        }
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                signature.trim().toLowerCase().getBytes(StandardCharsets.UTF_8))) {
            throw new BizException(403, "物流回调签名无效");
        }
    }

    @Transactional
    public void confirmReceipt(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BizException("订单不存在");
        orderLifecyclePort.markCompleted(CompleteCommand.builder().orderId(orderId).build());
    }

    public LogisticsVO getByOrderId(Long orderId) {
        LogisticsOrder lo = logisticsOrderMapper.selectOne(new LambdaQueryWrapper<LogisticsOrder>()
                .eq(LogisticsOrder::getOrderId, orderId)
                .orderByDesc(LogisticsOrder::getCreatedAt)
                .last("LIMIT 1"));
        if (lo == null) throw new BizException("物流单不存在");
        return toVO(lo);
    }

    private void updateOrderShipStatus(Order order) {
        List<OrderItem> allItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));
        List<LogisticsItem> shippedItems = logisticsItemMapper.selectList(new LambdaQueryWrapper<LogisticsItem>()
                .eq(LogisticsItem::getOrderId, order.getId()));

        int totalQty = allItems.stream().mapToInt(OrderItem::getQuantity).sum();
        int shippedQty = shippedItems.stream().mapToInt(LogisticsItem::getQty).sum();

        boolean allShipped = shippedQty >= totalQty;
        orderLifecyclePort.markShipped(ShipCommand.builder()
                .orderId(order.getId())
                .allShipped(allShipped)
                .build());
    }

    private LogisticsVO toVO(LogisticsOrder lo) {
        LogisticsVO vo = new LogisticsVO();
        vo.setId(lo.getId());
        vo.setOrderId(lo.getOrderId());
        vo.setLogisticsNo(lo.getLogisticsNo());
        vo.setProvider(lo.getProvider());
        vo.setTrackingNo(lo.getTrackingNo());
        vo.setStatus(lo.getStatus());
        vo.setShippedAt(lo.getShippedAt());
        vo.setDeliveredAt(lo.getDeliveredAt());
        vo.setExtra(lo.getExtra());
        vo.setCreatedAt(lo.getCreatedAt());
        vo.setItems(logisticsItemMapper.selectList(new LambdaQueryWrapper<LogisticsItem>()
                .eq(LogisticsItem::getLogisticsId, lo.getId()))
                .stream().map(li -> {
                    LogisticsVO.ItemVO iv = new LogisticsVO.ItemVO();
                    iv.setId(li.getId());
                    iv.setOrderItemId(li.getOrderItemId());
                    iv.setQty(li.getQty());
                    return iv;
                }).toList());
        return vo;
    }
}
