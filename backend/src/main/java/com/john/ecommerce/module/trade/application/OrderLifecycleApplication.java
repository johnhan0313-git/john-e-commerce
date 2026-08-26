package com.john.ecommerce.module.trade.application;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.john.ecommerce.common.enums.OrderStatus;
import com.john.ecommerce.common.enums.PayStatus;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.fulfillment.port.inventory.InventoryOrderRef;
import com.john.ecommerce.module.fulfillment.port.inventory.InventoryPort;
import com.john.ecommerce.module.trade.entity.Order;
import com.john.ecommerce.module.trade.mapper.OrderMapper;
import com.john.ecommerce.module.trade.port.OrderLifecyclePort;
import com.john.ecommerce.module.trade.port.command.*;
import com.john.ecommerce.module.trade.service.statemachine.OrderStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Sole writer of {@code t_order.status} and payment allocation fields on Order.
 * Inventory consume on pay is asynchronous via OrderPaid outbox (see CompletePaymentApplication).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderLifecycleApplication implements OrderLifecyclePort {

    public static final String EXTRA_STATUS_BEFORE_REFUND = "statusBeforeRefund";

    private final OrderMapper orderMapper;
    private final OrderStateMachine orderStateMachine;
    private final InventoryPort inventoryPort;

    @Override
    @Transactional
    public MarkPaidResult markPaid(MarkPaidCommand cmd) {
        Order order = requireOrder(cmd.getOrderId());
        if (order.getStatus() != null && order.getStatus() == OrderStatus.PAID.getCode()
                && cmd.getPayNo() != null && cmd.getPayNo().equals(order.getPayNo())) {
            return MarkPaidResult.none();
        }
        long paid = order.getPaidAmount() != null ? order.getPaidAmount() : 0L;
        long add = cmd.getAmount() != null ? cmd.getAmount() : 0L;
        long newPaid = paid + add;
        long payAmount = order.getPayAmount() != null ? order.getPayAmount() : 0L;
        boolean fullyPaid = newPaid >= payAmount;
        order.setPaidAmount(newPaid);
        order.setPayStatus(fullyPaid ? PayStatus.PAID.getCode() : PayStatus.PARTIAL.getCode());
        order.setPayNo(cmd.getPayNo());
        order.setPayTime(cmd.getPaidAt() != null ? cmd.getPaidAt() : System.currentTimeMillis());

        boolean publishOrderPaid = fullyPaid && order.getStatus() != null
                && order.getStatus() == OrderStatus.PENDING.getCode();
        if (publishOrderPaid) {
            transition(order, OrderStatus.PAID.getCode());
            orderMapper.updateById(order);
            return MarkPaidResult.builder()
                    .publishOrderPaid(true)
                    .orderId(order.getId())
                    .warehouseId(order.getWarehouseId())
                    .tenantId(order.getTenantId())
                    .build();
        }
        if (fullyPaid && order.getStatus() != null
                && order.getStatus() != OrderStatus.PENDING.getCode()) {
            log.warn("支付成功但订单非待支付，跳过状态推进与库存扣减 orderId={} status={}",
                    order.getId(), order.getStatus());
        }
        orderMapper.updateById(order);
        return MarkPaidResult.none();
    }

    @Override
    @Transactional
    public void startRefund(StartRefundCommand cmd) {
        Order order = requireOrder(cmd.getOrderId());
        if (order.getStatus() != null && order.getStatus() == OrderStatus.REFUNDING.getCode()) {
            return;
        }
        snapshotStatusBeforeRefund(order);
        transition(order, OrderStatus.REFUNDING.getCode());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void completeRefund(CompleteRefundCommand cmd) {
        Order order = requireOrder(cmd.getOrderId());
        if (order.getStatus() != null && order.getStatus() == OrderStatus.REFUNDED.getCode()) {
            return;
        }
        if (order.getStatus() == null || order.getStatus() != OrderStatus.REFUNDING.getCode()) {
            throw new BizException("订单当前不在退款中，无法完成退款");
        }
        transition(order, OrderStatus.REFUNDED.getCode());
        clearStatusBeforeRefund(order);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void restoreAfterRefund(RestoreAfterRefundCommand cmd) {
        Order order = requireOrder(cmd.getOrderId());
        if (order.getStatus() == null || order.getStatus() != OrderStatus.REFUNDING.getCode()) {
            return;
        }
        Integer target = cmd.getRestoreToStatus();
        if (target == null) {
            target = readStatusBeforeRefund(order);
        }
        if (target == null) {
            throw new BizException("缺少退款前状态快照，无法恢复订单状态");
        }
        transition(order, target);
        clearStatusBeforeRefund(order);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void markShipped(ShipCommand cmd) {
        Order order = requireOrder(cmd.getOrderId());
        int target = cmd.isAllShipped()
                ? OrderStatus.SHIPPED.getCode()
                : OrderStatus.PARTIAL_SHIPPED.getCode();
        if (order.getStatus() != null && order.getStatus() == target) {
            return;
        }
        transition(order, target);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void markDelivered(DeliverCommand cmd) {
        Order order = requireOrder(cmd.getOrderId());
        if (order.getStatus() != null && order.getStatus() == OrderStatus.DELIVERED.getCode()) {
            return;
        }
        if (order.getStatus() != null && order.getStatus() > OrderStatus.DELIVERED.getCode()
                && order.getStatus() != OrderStatus.PARTIAL_SHIPPED.getCode()
                && order.getStatus() != OrderStatus.SHIPPED.getCode()) {
            return;
        }
        transition(order, OrderStatus.DELIVERED.getCode());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void markCompleted(CompleteCommand cmd) {
        Order order = requireOrder(cmd.getOrderId());
        if (order.getStatus() != null && order.getStatus() == OrderStatus.COMPLETED.getCode()) {
            return;
        }
        transition(order, OrderStatus.COMPLETED.getCode());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void cancel(CancelCommand cmd) {
        Order order = requireOrder(cmd.getOrderId());
        if (order.getStatus() != null && order.getStatus() == OrderStatus.CANCELLED.getCode()) {
            return;
        }
        transition(order, OrderStatus.CANCELLED.getCode());
        if (cmd.isUnlockInventory()) {
            inventoryPort.unlockForOrder(new InventoryOrderRef(order.getId(), order.getWarehouseId()));
        }
        order.setCancelTime(System.currentTimeMillis());
        order.setCancelReason(cmd.getReason());
        order.setCancelBy(cmd.getCancelBy());
        orderMapper.updateById(order);
    }

    /**
     * Conditional status update used by timeout job to avoid races with concurrent pay.
     */
    @Transactional
    public boolean cancelIfStillPendingUnpaid(Long orderId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) return false;
        if (order.getStatus() == null || order.getStatus() != OrderStatus.PENDING.getCode()) return false;
        if (order.getPayStatus() == null || order.getPayStatus() != PayStatus.UNPAID.getCode()) return false;
        long now = System.currentTimeMillis();
        if (order.getPayDeadline() == null || order.getPayDeadline() >= now) return false;
        orderStateMachine.assertTransition(order.getStatus(), OrderStatus.CANCELLED.getCode());
        inventoryPort.unlockForOrder(new InventoryOrderRef(order.getId(), order.getWarehouseId()));
        int updated = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getStatus, OrderStatus.PENDING.getCode())
                .eq(Order::getPayStatus, PayStatus.UNPAID.getCode())
                .set(Order::getStatus, OrderStatus.CANCELLED.getCode())
                .set(Order::getCancelTime, now)
                .set(Order::getCancelReason, reason));
        return updated > 0;
    }

    private void transition(Order order, int toStatus) {
        orderStateMachine.assertTransition(order.getStatus(), toStatus);
        order.setStatus(toStatus);
    }

    private Order requireOrder(Long orderId) {
        if (orderId == null) throw new BizException("订单ID不能为空");
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BizException("订单不存在");
        return order;
    }

    private void snapshotStatusBeforeRefund(Order order) {
        Map<String, Object> extra = order.getExtra() != null ? new HashMap<>(order.getExtra()) : new HashMap<>();
        extra.put(EXTRA_STATUS_BEFORE_REFUND, order.getStatus());
        order.setExtra(extra);
    }

    private Integer readStatusBeforeRefund(Order order) {
        if (order.getExtra() == null) return null;
        Object v = order.getExtra().get(EXTRA_STATUS_BEFORE_REFUND);
        if (v instanceof Number n) return n.intValue();
        if (v instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private void clearStatusBeforeRefund(Order order) {
        if (order.getExtra() == null) return;
        Map<String, Object> extra = new HashMap<>(order.getExtra());
        extra.remove(EXTRA_STATUS_BEFORE_REFUND);
        order.setExtra(extra.isEmpty() ? null : extra);
    }
}
