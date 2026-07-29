package com.john.ecommerce.module.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.context.UserContext;
import com.john.ecommerce.common.enums.OrderStatus;
import com.john.ecommerce.common.enums.PayStatus;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.activity.service.engine.PromoContext;
import com.john.ecommerce.module.activity.service.engine.PromoEngine;
import com.john.ecommerce.module.activity.service.engine.PromoOrderResult;
import com.john.ecommerce.module.fulfillment.service.inventory.InventoryFacade;
import com.john.ecommerce.module.product.entity.Sku;
import com.john.ecommerce.module.product.entity.Spu;
import com.john.ecommerce.module.product.mapper.SkuMapper;
import com.john.ecommerce.module.product.mapper.SpuMapper;
import com.john.ecommerce.module.trade.dto.OrderCreateDTO;
import com.john.ecommerce.module.trade.dto.OrderGroupVO;
import com.john.ecommerce.module.trade.dto.OrderVO;
import com.john.ecommerce.module.trade.entity.Order;
import com.john.ecommerce.module.trade.entity.OrderItem;
import com.john.ecommerce.module.trade.mapper.OrderItemMapper;
import com.john.ecommerce.module.trade.mapper.OrderMapper;
import com.john.ecommerce.module.trade.service.split.OrderSplitBucket;
import com.john.ecommerce.module.trade.service.split.OrderSplitter;
import com.john.ecommerce.module.trade.service.statemachine.OrderStateMachine;
import com.john.ecommerce.module.user.entity.UserAddress;
import com.john.ecommerce.module.user.mapper.UserAddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SkuMapper skuMapper;
    private final SpuMapper spuMapper;
    private final UserAddressMapper addressMapper;
    private final OrderSplitter orderSplitter;
    private final OrderStateMachine orderStateMachine;
    private final InventoryFacade inventoryFacade;
    private final ObjectProvider<PromoEngine> promoEngineProvider;

    @Transactional
    public OrderGroupVO create(OrderCreateDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw new BizException("用户未登录");

        fillReceiverFromAddress(dto, userId);

        PromoOrderResult promo = applyPromo(userId, dto);
        Map<Long, PromoOrderResult.PromoLineResult> promoLineMap = promo.getLines().stream()
                .collect(Collectors.toMap(PromoOrderResult.PromoLineResult::getSkuId, l -> l, (a, b) -> a));

        List<OrderSplitBucket.SplitLine> splitLines = new ArrayList<>();
        for (OrderCreateDTO.OrderItemDTO itemDTO : dto.getItems()) {
            Sku sku = skuMapper.selectById(itemDTO.getSkuId());
            if (sku == null) throw new BizException("SKU不存在: " + itemDTO.getSkuId());
            Spu spu = spuMapper.selectById(sku.getSpuId());
            if (spu == null) throw new BizException("商品不存在");

            PromoOrderResult.PromoLineResult pl = promoLineMap.get(sku.getId());
            BigDecimal unitPrice = pl != null ? pl.getUnitPrice() : sku.getPrice();
            BigDecimal lineTotal = pl != null ? pl.getLineTotal() : sku.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            BigDecimal discount = pl != null ? pl.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal pay = pl != null ? pl.getPayAmount() : lineTotal;

            OrderSplitBucket.SplitLine line = new OrderSplitBucket.SplitLine();
            line.setSku(sku);
            line.setSpu(spu);
            line.setQuantity(itemDTO.getQuantity());
            line.setUnitPrice(unitPrice);
            line.setDiscountAmount(discount);
            line.setPayAmount(pay);
            line.setActivityId(resolveActivityId(dto));
            splitLines.add(line);
        }

        List<OrderSplitBucket> buckets = orderSplitter.split(splitLines);
        String orderGroupNo = generateGroupNo();
        List<OrderVO> orderVOs = new ArrayList<>();

        for (OrderSplitBucket bucket : buckets) {
            BigDecimal total = bucket.getLines().stream().map(l -> l.getUnitPrice()
                    .multiply(BigDecimal.valueOf(l.getQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal discount = bucket.getLines().stream().map(OrderSplitBucket.SplitLine::getDiscountAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal pay = bucket.getLines().stream().map(OrderSplitBucket.SplitLine::getPayAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Order order = new Order();
            order.setOrderGroupNo(orderGroupNo);
            order.setOrderNo(generateOrderNo());
            order.setUserId(userId);
            order.setMerchantId(bucket.getMerchantId());
            order.setWarehouseId(bucket.getWarehouseId());
            order.setSplitReason(bucket.getSplitReason());
            order.setOrderType(dto.getOrderType() != null ? dto.getOrderType() : 0);
            order.setStatus(OrderStatus.PENDING.getCode());
            order.setTotalAmount(total);
            order.setDiscountAmount(discount);
            order.setPayAmount(pay);
            order.setPaidAmount(BigDecimal.ZERO);
            order.setPayStatus(PayStatus.UNPAID.getCode());
            order.setReceiverName(dto.getReceiverName());
            order.setReceiverPhone(dto.getReceiverPhone());
            order.setReceiverAddress(dto.getReceiverAddress());
            order.setRemark(dto.getRemark());
            order.setActivityId(resolveActivityId(dto));
            order.setCampaignId(dto.getCampaignId());
            order.setDiningType(dto.getDiningType());
            order.setTableNo(dto.getTableNo());
            orderMapper.insert(order);

            List<OrderItem> items = new ArrayList<>();
            for (OrderSplitBucket.SplitLine line : bucket.getLines()) {
                OrderItem item = new OrderItem();
                item.setOrderId(order.getId());
                item.setSpuId(line.getSpu().getId());
                item.setSkuId(line.getSku().getId());
                item.setSkuName(line.getSku().getSkuName());
                item.setSpecValues(line.getSku().getSpecValues());
                item.setPrice(line.getUnitPrice());
                item.setQuantity(line.getQuantity());
                item.setSubtotal(line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
                item.setDiscountAmount(line.getDiscountAmount());
                item.setPayAmount(line.getPayAmount());
                orderItemMapper.insert(item);
                items.add(item);
            }

            inventoryFacade.lockForOrder(order, items);
            orderVOs.add(toVO(order, items));
        }

        OrderGroupVO group = new OrderGroupVO();
        group.setOrderGroupNo(orderGroupNo);
        group.setOrderCount(orderVOs.size());
        group.setTotalAmount(promo.getTotalAmount());
        group.setDiscountAmount(promo.getDiscountAmount());
        group.setPayAmount(promo.getPayAmount());
        group.setGroupStatus(deriveGroupStatus(orderVOs));
        group.setOrders(orderVOs);
        return group;
    }

    public OrderGroupVO getGroup(String orderGroupNo) {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderGroupNo, orderGroupNo)
                .orderByAsc(Order::getId));
        if (orders.isEmpty()) throw new BizException("订单组不存在");

        List<OrderVO> vos = orders.stream().map(o -> {
            List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                    .eq(OrderItem::getOrderId, o.getId()));
            return toVO(o, items);
        }).toList();

        OrderGroupVO group = new OrderGroupVO();
        group.setOrderGroupNo(orderGroupNo);
        group.setOrderCount(vos.size());
        group.setTotalAmount(vos.stream().map(OrderVO::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        group.setDiscountAmount(vos.stream().map(OrderVO::getDiscountAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        group.setPayAmount(vos.stream().map(OrderVO::getPayAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        group.setGroupStatus(deriveGroupStatus(vos));
        group.setOrders(vos);
        return group;
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
        orderStateMachine.assertTransition(order.getStatus(), status);
        if (status == OrderStatus.CANCELLED.getCode()) {
            inventoryFacade.unlockForOrder(order);
            order.setCancelTime(System.currentTimeMillis());
            order.setCancelBy(UserContext.getCurrentUserId());
            if (order.getCancelReason() == null) {
                order.setCancelReason("用户取消");
            }
        }
        order.setStatus(status);
        orderMapper.updateById(order);
    }

    private PromoOrderResult applyPromo(Long userId, OrderCreateDTO dto) {
        PromoEngine promoEngine = promoEngineProvider.getIfAvailable();
        if (promoEngine == null) {
            return buildFallbackPromo(dto);
        }
        PromoContext context = new PromoContext();
        context.setUserId(userId);
        for (OrderCreateDTO.OrderItemDTO itemDTO : dto.getItems()) {
            Sku sku = skuMapper.selectById(itemDTO.getSkuId());
            if (sku == null) throw new BizException("SKU不存在: " + itemDTO.getSkuId());
            Spu spu = spuMapper.selectById(sku.getSpuId());
            PromoContext.PromoLine line = new PromoContext.PromoLine();
            line.setSkuId(sku.getId());
            line.setSpuId(spu.getId());
            line.setCategoryId(spu.getCategoryId());
            line.setMerchantId(spu.getMerchantId());
            line.setQuantity(itemDTO.getQuantity());
            line.setUnitPrice(sku.getPrice());
            line.setLineTotal(sku.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            context.getLines().add(line);
        }
        return promoEngine.preview(context);
    }

    private PromoOrderResult buildFallbackPromo(OrderCreateDTO dto) {
        PromoOrderResult result = new PromoOrderResult();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderCreateDTO.OrderItemDTO itemDTO : dto.getItems()) {
            Sku sku = skuMapper.selectById(itemDTO.getSkuId());
            BigDecimal lineTotal = sku.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            PromoOrderResult.PromoLineResult lr = new PromoOrderResult.PromoLineResult();
            lr.setSkuId(sku.getId());
            lr.setSpuId(sku.getSpuId());
            lr.setQuantity(itemDTO.getQuantity());
            lr.setUnitPrice(sku.getPrice());
            lr.setLineTotal(lineTotal);
            lr.setDiscountAmount(BigDecimal.ZERO);
            lr.setPayAmount(lineTotal);
            result.getLines().add(lr);
            total = total.add(lineTotal);
        }
        result.setTotalAmount(total);
        result.setDiscountAmount(BigDecimal.ZERO);
        result.setPayAmount(total);
        return result;
    }

    private void fillReceiverFromAddress(OrderCreateDTO dto, Long userId) {
        if (dto.getAddressId() == null) return;
        UserAddress addr = addressMapper.selectById(dto.getAddressId());
        if (addr == null || !userId.equals(addr.getUserId())) {
            throw new BizException("收货地址不存在");
        }
        dto.setReceiverName(addr.getName());
        dto.setReceiverPhone(addr.getPhone());
        dto.setReceiverAddress(String.join("",
                nullToEmpty(addr.getProvince()),
                nullToEmpty(addr.getCity()),
                nullToEmpty(addr.getDistrict()),
                nullToEmpty(addr.getDetail())));
    }

    private Long resolveActivityId(OrderCreateDTO dto) {
        if (dto.getActivityId() != null) return dto.getActivityId();
        return dto.getCampaignId();
    }

    private String deriveGroupStatus(List<OrderVO> orders) {
        boolean allPending = orders.stream().allMatch(o -> o.getStatus() == OrderStatus.PENDING.getCode());
        if (allPending) return "GROUP_PENDING";
        boolean allCompleted = orders.stream().allMatch(o -> o.getStatus() == OrderStatus.COMPLETED.getCode());
        if (allCompleted) return "GROUP_COMPLETED";
        boolean anyCancelled = orders.stream().anyMatch(o -> o.getStatus() == OrderStatus.CANCELLED.getCode());
        if (anyCancelled) return "GROUP_PARTIAL";
        return "GROUP_IN_PROGRESS";
    }

    private String generateGroupNo() {
        return "OG" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }

    private String generateOrderNo() {
        return "EC" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private OrderVO toVO(Order o, List<OrderItem> items) {
        OrderVO vo = new OrderVO();
        vo.setId(o.getId());
        vo.setOrderGroupNo(o.getOrderGroupNo());
        vo.setOrderNo(o.getOrderNo());
        vo.setUserId(o.getUserId());
        vo.setMerchantId(o.getMerchantId());
        vo.setWarehouseId(o.getWarehouseId());
        vo.setOrderType(o.getOrderType());
        vo.setStatus(o.getStatus());
        vo.setStatusLabel(getStatusLabel(o.getStatus()));
        vo.setSplitReason(o.getSplitReason());
        vo.setTotalAmount(o.getTotalAmount());
        vo.setDiscountAmount(o.getDiscountAmount());
        vo.setPayAmount(o.getPayAmount());
        vo.setPaidAmount(o.getPaidAmount());
        vo.setPayStatus(o.getPayStatus());
        vo.setPayStatusLabel(getPayStatusLabel(o.getPayStatus()));
        vo.setPayType(o.getPayType());
        vo.setPayTime(o.getPayTime());
        vo.setReceiverName(o.getReceiverName());
        vo.setReceiverPhone(o.getReceiverPhone());
        vo.setReceiverAddress(o.getReceiverAddress());
        vo.setRemark(o.getRemark());
        vo.setActivityId(o.getActivityId() != null ? o.getActivityId() : o.getCampaignId());
        vo.setCancelTime(o.getCancelTime());
        vo.setCancelReason(o.getCancelReason());
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
        vo.setDiscountAmount(item.getDiscountAmount());
        vo.setPayAmount(item.getPayAmount());
        return vo;
    }

    private String getStatusLabel(int status) {
        for (OrderStatus s : OrderStatus.values()) {
            if (s.getCode() == status) return s.getLabel();
        }
        return "未知";
    }

    private String getPayStatusLabel(Integer payStatus) {
        if (payStatus == null) return "未知";
        for (PayStatus s : PayStatus.values()) {
            if (s.getCode() == payStatus) return s.getLabel();
        }
        return "未知";
    }
}
