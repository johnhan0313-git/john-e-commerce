package com.john.ecommerce.module.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.context.UserContext;
import com.john.ecommerce.common.enums.OrderStatus;
import com.john.ecommerce.module.user.identity.IdentityCodes;
import com.john.ecommerce.common.enums.PayStatus;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.trade.dto.OrderGroupVO;
import com.john.ecommerce.module.trade.dto.OrderVO;
import com.john.ecommerce.module.trade.entity.Order;
import com.john.ecommerce.module.trade.entity.OrderItem;
import com.john.ecommerce.module.trade.mapper.OrderItemMapper;
import com.john.ecommerce.module.trade.mapper.OrderMapper;
import com.john.ecommerce.module.trade.application.OrderLifecycleApplication;
import com.john.ecommerce.module.trade.port.OrderLifecyclePort;
import com.john.ecommerce.module.trade.port.command.CancelCommand;
import com.john.ecommerce.module.trade.service.statemachine.OrderStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStateMachine orderStateMachine;
    private final OrderLifecyclePort orderLifecyclePort;
    private final OrderLifecycleApplication orderLifecycleApplication;

    public OrderGroupVO getGroup(String orderGroupNo) {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderGroupNo, orderGroupNo)
                .orderByAsc(Order::getId));
        if (orders.isEmpty()) throw new BizException("订单组不存在");
        if (!isOps()) {
            Long userId = requireUserId();
            if (orders.stream().anyMatch(order -> !userId.equals(order.getUserId()))) {
                throw new BizException(403, "无权访问该订单组");
            }
        }

        List<OrderVO> vos = orders.stream().map(o -> {
            List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                    .eq(OrderItem::getOrderId, o.getId()));
            return toVO(o, items);
        }).toList();

        OrderGroupVO group = new OrderGroupVO();
        group.setOrderGroupNo(orderGroupNo);
        group.setOrderCount(vos.size());
        group.setTotalAmount(vos.stream().map(OrderVO::getTotalAmount).map(a -> a != null ? a : 0L).reduce(0L, Long::sum));
        group.setDiscountAmount(vos.stream().map(OrderVO::getDiscountAmount).map(a -> a != null ? a : 0L).reduce(0L, Long::sum));
        group.setPayAmount(vos.stream().map(OrderVO::getPayAmount).map(a -> a != null ? a : 0L).reduce(0L, Long::sum));
        group.setGroupStatus(deriveGroupStatus(vos));
        group.setOrders(vos);
        return group;
    }

    public OrderVO getById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BizException("订单不存在");
        if (!isOps() && !requireUserId().equals(order.getUserId())) {
            throw new BizException(403, "无权访问该订单");
        }
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        return toVO(order, items);
    }

    public OrderVO getByIdForShop(Long id, Long shopId) {
        Order order = orderMapper.selectById(id);
        if (order == null || shopId == null || !shopId.equals(order.getShopId())) {
            throw new BizException("订单不属于当前店铺");
        }
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        return toVO(order, items);
    }

    public Page<OrderVO> list(int page, int size, Integer status) {
        return list(page, size, status, null, null, true);
    }

    public Page<OrderVO> listForShop(int page, int size, Integer status, Long shopId) {
        return list(page, size, status, shopId, null, false);
    }

    public Page<OrderVO> list(int page, int size, Integer status, Long shopId, Long merchantId, boolean buyerScoped) {
        Long userId = UserContext.getCurrentUserId();
        if (!buyerScoped && !isOps()) {
            throw new BizException(403, "无权查询全部订单");
        }
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(buyerScoped && userId != null, Order::getUserId, userId)
                .eq(status != null, Order::getStatus, status)
                .eq(shopId != null, Order::getShopId, shopId)
                .eq(merchantId != null, Order::getMerchantId, merchantId)
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
        if (!isOps()) {
            Long userId = requireUserId();
            if (!userId.equals(order.getUserId())) {
                throw new BizException(403, "无权操作该订单");
            }
            if (status == null || status != OrderStatus.CANCELLED.getCode()) {
                throw new BizException(403, "买家只能取消自己的订单");
            }
        }
        if (status == OrderStatus.CANCELLED.getCode()) {
            orderLifecyclePort.cancel(CancelCommand.builder()
                    .orderId(id)
                    .reason(order.getCancelReason() != null ? order.getCancelReason() : "用户取消")
                    .cancelBy(UserContext.getCurrentUserId())
                    .unlockInventory(true)
                    .build());
            return;
        }
        orderStateMachine.assertTransition(order.getStatus(), status);
        order.setStatus(status);
        orderMapper.updateById(order);
    }

    private boolean isOps() {
        return UserContext.hasIdentity(IdentityCodes.OPS);
    }

    private Long requireUserId() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw new BizException(401, "用户未登录");
        return userId;
    }

    /**
     * Cancel unpaid orders past pay_deadline and unlock stock. Returns cancelled count.
     */
    @Transactional
    public int cancelExpiredUnpaidOrders(int limit) {
        long now = System.currentTimeMillis();
        int batch = Math.min(Math.max(limit, 1), 200);
        List<Order> expired = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, OrderStatus.PENDING.getCode())
                .eq(Order::getPayStatus, PayStatus.UNPAID.getCode())
                .isNotNull(Order::getPayDeadline)
                .lt(Order::getPayDeadline, now)
                .orderByAsc(Order::getPayDeadline)
                .last("LIMIT " + batch));
        int cancelled = 0;
        for (Order candidate : expired) {
            if (cancelForPayTimeout(candidate.getId())) {
                cancelled++;
            }
        }
        return cancelled;
    }

    @Transactional
    public boolean cancelForPayTimeout(Long id) {
        return orderLifecycleApplication.cancelIfStillPendingUnpaid(id, "PAY_TIMEOUT");
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
