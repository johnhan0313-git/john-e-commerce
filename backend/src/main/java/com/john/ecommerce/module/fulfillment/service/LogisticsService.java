package com.john.ecommerce.module.fulfillment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.enums.OrderStatus;
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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogisticsService {

    private final LogisticsOrderMapper logisticsOrderMapper;
    private final LogisticsItemMapper logisticsItemMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ObjectProvider<OrderCompleteHandler> orderCompleteHandlers;

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
    public void webhook(String trackingNo, LogisticsWebhookDTO dto) {
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
                    Order order = orderMapper.selectById(lo.getOrderId());
                    if (order != null && order.getStatus() < OrderStatus.DELIVERED.getCode()) {
                        order.setStatus(OrderStatus.DELIVERED.getCode());
                        orderMapper.updateById(order);
                    }
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

    /**
     * Called when buyer confirms receipt. Transitions order to COMPLETED
     * and notifies downstream (e.g. settlement) via OrderCompleteHandler.
     */
    @Transactional
    public void confirmReceipt(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BizException("订单不存在");
        if (order.getStatus() != OrderStatus.DELIVERED.getCode()
                && order.getStatus() != OrderStatus.SHIPPED.getCode()) {
            throw new BizException("当前订单状态不可确认收货");
        }
        order.setStatus(OrderStatus.COMPLETED.getCode());
        orderMapper.updateById(order);

        orderCompleteHandlers.forEach(h -> h.onOrderComplete(orderId));
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

        if (shippedQty >= totalQty) {
            order.setStatus(OrderStatus.SHIPPED.getCode());
        } else if (shippedQty > 0) {
            order.setStatus(OrderStatus.PARTIAL_SHIPPED.getCode());
        }
        orderMapper.updateById(order);
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
