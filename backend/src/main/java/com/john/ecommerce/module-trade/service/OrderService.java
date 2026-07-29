package com.john.ecommerce.module-trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.context.UserContext;
import com.john.ecommerce.common.enums.OrderStatus;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module-product.entity.Sku;
import com.john.ecommerce.module-product.entity.Spu;
import com.john.ecommerce.module-product.mapper.SkuMapper;
import com.john.ecommerce.module-product.mapper.SpuMapper;
import com.john.ecommerce.module-trade.dto.OrderCreateDTO;
import com.john.ecommerce.module-trade.dto.OrderVO;
import com.john.ecommerce.module-trade.entity.Order;
import com.john.ecommerce.module-trade.entity.OrderItem;
import com.john.ecommerce.module-trade.mapper.OrderItemMapper;
import com.john.ecommerce.module-trade.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SkuMapper skuMapper;
    private final SpuMapper spuMapper;

    @Transactional
    public OrderVO create(OrderCreateDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw new BizException("用户未登录");

        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderCreateDTO.OrderItemDTO itemDTO : dto.getItems()) {
            Sku sku = skuMapper.selectById(itemDTO.getSkuId());
            if (sku == null) throw new BizException("SKU不存在: " + itemDTO.getSkuId());
            if (sku.getStock() < itemDTO.getQuantity()) throw new BizException("库存不足: " + sku.getSkuName());

            Spu spu = spuMapper.selectById(sku.getSpuId());
            if (spu == null) throw new BizException("商品不存在");

            BigDecimal subtotal = sku.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem item = new OrderItem();
            item.setSpuId(sku.getSpuId());
            item.setSkuId(sku.getId());
            item.setSkuName(sku.getSkuName());
            item.setSpecValues(sku.getSpecValues());
            item.setPrice(sku.getPrice());
            item.setQuantity(itemDTO.getQuantity());
            item.setSubtotal(subtotal);
            items.add(item);
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setOrderType(dto.getOrderType() != null ? dto.getOrderType() : 0);
        order.setStatus(OrderStatus.PENDING.getCode());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(totalAmount);
        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setReceiverAddress(dto.getReceiverAddress());
        order.setRemark(dto.getRemark());
        order.setCampaignId(dto.getCampaignId());
        order.setDiningType(dto.getDiningType());
        order.setTableNo(dto.getTableNo());
        orderMapper.insert(order);

        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        for (OrderCreateDTO.OrderItemDTO itemDTO : dto.getItems()) {
            Sku sku = skuMapper.selectById(itemDTO.getSkuId());
            sku.setStock(sku.getStock() - itemDTO.getQuantity());
            skuMapper.updateById(sku);
        }

        return toVO(order, items);
    }

    public OrderVO getById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BizException("订单不存在");
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        return toVO(order, items);
    }

    public Page<OrderVO> list(int page, int size, Integer status) {
        Long userId = UserContext.getCurrentUserId();
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(userId != null, Order::getUserId, userId)
                .eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getCreatedAt);
        Page<Order> p = orderMapper.selectPage(new Page<>(page, size), wrapper);

        Page<OrderVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(o -> {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getId()));
            return toVO(o, items);
        }).toList());
        return result;
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BizException("订单不存在");
        order.setStatus(status);
        orderMapper.updateById(order);
    }

    private String generateOrderNo() {
        return "EC" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }

    private OrderVO toVO(Order o, List<OrderItem> items) {
        OrderVO vo = new OrderVO();
        vo.setId(o.getId());
        vo.setOrderNo(o.getOrderNo());
        vo.setUserId(o.getUserId());
        vo.setMerchantId(o.getMerchantId());
        vo.setOrderType(o.getOrderType());
        vo.setStatus(o.getStatus());
        vo.setStatusLabel(getStatusLabel(o.getStatus()));
        vo.setTotalAmount(o.getTotalAmount());
        vo.setDiscountAmount(o.getDiscountAmount());
        vo.setPayAmount(o.getPayAmount());
        vo.setPayType(o.getPayType());
        vo.setPayTime(o.getPayTime());
        vo.setReceiverName(o.getReceiverName());
        vo.setReceiverPhone(o.getReceiverPhone());
        vo.setReceiverAddress(o.getReceiverAddress());
        vo.setRemark(o.getRemark());
        vo.setCampaignId(o.getCampaignId());
        vo.setExtra(o.getExtra());
        vo.setCreatedAt(o.getCreatedAt());
        vo.setItems(items.stream().map(this::toItemVO).toList());
        return vo;
    }

    private OrderVO.OrderItemVO toItemVO(OrderItem item) {
        OrderVO.OrderItemVO vo = new OrderVO.OrderItemVO();
        vo.setId(item.getId());
        vo.setSpuId(item.getSpuId());
        vo.setSkuId(item.getSkuId());
        vo.setSkuName(item.getSkuName());
        vo.setSkuImage(item.getSkuImage());
        vo.setSpecValues(item.getSpecValues());
        vo.setPrice(item.getPrice());
        vo.setQuantity(item.getQuantity());
        vo.setSubtotal(item.getSubtotal());
        return vo;
    }

    private String getStatusLabel(int status) {
        for (OrderStatus s : OrderStatus.values()) {
            if (s.getCode() == status) return s.getLabel();
        }
        return "未知";
    }
}
