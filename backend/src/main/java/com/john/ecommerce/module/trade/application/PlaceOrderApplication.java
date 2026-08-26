package com.john.ecommerce.module.trade.application;

import com.john.ecommerce.common.context.UserContext;
import com.john.ecommerce.common.enums.OrderStatus;
import com.john.ecommerce.common.enums.PayStatus;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.config.AppProperties;
import com.john.ecommerce.module.activity.service.engine.PromoContext;
import com.john.ecommerce.module.activity.service.engine.PromoEngine;
import com.john.ecommerce.module.activity.service.engine.PromoOrderResult;
import com.john.ecommerce.module.fulfillment.port.inventory.InventoryLine;
import com.john.ecommerce.module.fulfillment.port.inventory.InventoryLockCommand;
import com.john.ecommerce.module.fulfillment.port.inventory.InventoryPort;
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
import com.john.ecommerce.module.user.entity.UserAddress;
import com.john.ecommerce.module.user.mapper.UserAddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Place-order orchestration: promo → split → persist → lock inventory.
 */
@Service
@RequiredArgsConstructor
public class PlaceOrderApplication {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SkuMapper skuMapper;
    private final SpuMapper spuMapper;
    private final UserAddressMapper addressMapper;
    private final OrderSplitter orderSplitter;
    private final InventoryPort inventoryPort;
    private final ObjectProvider<PromoEngine> promoEngineProvider;
    private final AppProperties appProperties;

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
            long unitPrice = pl != null && pl.getUnitPrice() != null
                    ? pl.getUnitPrice()
                    : (sku.getPrice() != null ? sku.getPrice() : 0L);
            long lineTotal = pl != null && pl.getLineTotal() != null
                    ? pl.getLineTotal()
                    : unitPrice * itemDTO.getQuantity();
            long discount = pl != null && pl.getDiscountAmount() != null ? pl.getDiscountAmount() : 0L;
            long pay = pl != null && pl.getPayAmount() != null ? pl.getPayAmount() : lineTotal;

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
            long total = bucket.getLines().stream()
                    .mapToLong(l -> (l.getUnitPrice() != null ? l.getUnitPrice() : 0L) * l.getQuantity())
                    .sum();
            long discount = bucket.getLines().stream()
                    .map(OrderSplitBucket.SplitLine::getDiscountAmount)
                    .map(d -> d != null ? d : 0L)
                    .reduce(0L, Long::sum);
            long pay = bucket.getLines().stream()
                    .map(OrderSplitBucket.SplitLine::getPayAmount)
                    .map(p -> p != null ? p : 0L)
                    .reduce(0L, Long::sum);

            Order order = new Order();
            order.setOrderGroupNo(orderGroupNo);
            order.setOrderNo(generateOrderNo());
            order.setUserId(userId);
            order.setMerchantId(bucket.getMerchantId());
            order.setShopId(bucket.getShopId());
            order.setWarehouseId(bucket.getWarehouseId());
            order.setSplitReason(bucket.getSplitReason());
            order.setOrderType(dto.getOrderType() != null ? dto.getOrderType() : 0);
            order.setStatus(OrderStatus.PENDING.getCode());
            order.setTotalAmount(total);
            order.setDiscountAmount(discount);
            order.setPayAmount(pay);
            order.setPaidAmount(0L);
            order.setPayStatus(PayStatus.UNPAID.getCode());
            long now = System.currentTimeMillis();
            long payTimeoutMs = appProperties.getTrade() != null
                    ? appProperties.getTrade().getPayTimeoutMs() : 30L * 60 * 1000;
            order.setPayDeadline(now + Math.max(payTimeoutMs, 0));
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
            List<InventoryLine> lockLines = new ArrayList<>();
            for (OrderSplitBucket.SplitLine line : bucket.getLines()) {
                OrderItem item = new OrderItem();
                item.setOrderId(order.getId());
                item.setSpuId(line.getSpu().getId());
                item.setSkuId(line.getSku().getId());
                item.setSkuName(line.getSku().getSkuName());
                item.setSpecValues(line.getSku().getSpecValues());
                item.setPrice(line.getUnitPrice());
                item.setQuantity(line.getQuantity());
                long unit = line.getUnitPrice() != null ? line.getUnitPrice() : 0L;
                item.setSubtotal(unit * line.getQuantity());
                item.setDiscountAmount(line.getDiscountAmount());
                item.setPayAmount(line.getPayAmount());
                orderItemMapper.insert(item);
                items.add(item);
                lockLines.add(new InventoryLine(item.getSkuId(),
                        item.getQuantity() != null ? item.getQuantity() : 0));
            }

            inventoryPort.lockForOrder(new InventoryLockCommand(order.getId(), order.getWarehouseId(), lockLines));
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
            long unit = sku.getPrice() != null ? sku.getPrice() : 0L;
            line.setLineTotal(unit * itemDTO.getQuantity());
            context.getLines().add(line);
        }
        return promoEngine.preview(context);
    }

    private PromoOrderResult buildFallbackPromo(OrderCreateDTO dto) {
        PromoOrderResult result = new PromoOrderResult();
        long total = 0L;
        for (OrderCreateDTO.OrderItemDTO itemDTO : dto.getItems()) {
            Sku sku = skuMapper.selectById(itemDTO.getSkuId());
            long unit = sku.getPrice() != null ? sku.getPrice() : 0L;
            long lineTotal = unit * itemDTO.getQuantity();
            PromoOrderResult.PromoLineResult lr = new PromoOrderResult.PromoLineResult();
            lr.setSkuId(sku.getId());
            lr.setSpuId(sku.getSpuId());
            lr.setQuantity(itemDTO.getQuantity());
            lr.setUnitPrice(sku.getPrice());
            lr.setLineTotal(lineTotal);
            lr.setDiscountAmount(0L);
            lr.setPayAmount(lineTotal);
            result.getLines().add(lr);
            total += lineTotal;
        }
        result.setTotalAmount(total);
        result.setDiscountAmount(0L);
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
        vo.setShopId(o.getShopId());
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
        vo.setPayDeadline(o.getPayDeadline());
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
