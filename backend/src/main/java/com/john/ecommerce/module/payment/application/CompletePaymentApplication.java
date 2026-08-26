package com.john.ecommerce.module.payment.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.john.ecommerce.common.outbox.OutboxEventTypes;
import com.john.ecommerce.common.outbox.OutboxPort;
import com.john.ecommerce.module.payment.entity.Payment;
import com.john.ecommerce.module.payment.entity.PaymentItem;
import com.john.ecommerce.module.payment.enums.PaymentStatus;
import com.john.ecommerce.module.payment.mapper.PaymentItemMapper;
import com.john.ecommerce.module.payment.mapper.PaymentMapper;
import com.john.ecommerce.module.payment.service.SettlementBillService;
import com.john.ecommerce.module.trade.port.OrderLifecyclePort;
import com.john.ecommerce.module.trade.port.command.MarkPaidCommand;
import com.john.ecommerce.module.trade.port.command.MarkPaidResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Idempotent payment completion: claim SUCCESS → markPaid → OrderPaid outbox → settlement (sync).
 */
@Service
@RequiredArgsConstructor
public class CompletePaymentApplication {

    private final PaymentMapper paymentMapper;
    private final PaymentItemMapper paymentItemMapper;
    private final OrderLifecyclePort orderLifecyclePort;
    private final SettlementBillService settlementBillService;
    private final OutboxPort outboxPort;

    @Transactional
    public void complete(Payment payment) {
        long paidAt = System.currentTimeMillis();
        int claimed = paymentMapper.update(null, new LambdaUpdateWrapper<Payment>()
                .eq(Payment::getId, payment.getId())
                .in(Payment::getStatus, PaymentStatus.PENDING.getCode(), PaymentStatus.PROCESSING.getCode())
                .set(Payment::getStatus, PaymentStatus.SUCCESS.getCode())
                .set(Payment::getPaidAt, paidAt));
        if (claimed == 0) return;
        payment.setStatus(PaymentStatus.SUCCESS.getCode());
        payment.setPaidAt(paidAt);

        List<PaymentItem> items = paymentItemMapper.selectList(new LambdaQueryWrapper<PaymentItem>()
                .eq(PaymentItem::getPaymentId, payment.getId()));

        for (PaymentItem item : items) {
            MarkPaidResult paid = orderLifecyclePort.markPaid(MarkPaidCommand.builder()
                    .orderId(item.getOrderId())
                    .amount(item.getAmount())
                    .payNo(payment.getPayNo())
                    .paidAt(payment.getPaidAt())
                    .build());
            if (paid.isPublishOrderPaid()) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("orderId", paid.getOrderId());
                payload.put("warehouseId", paid.getWarehouseId());
                Long tenantId = paid.getTenantId() != null ? paid.getTenantId() : payment.getTenantId();
                outboxPort.append(
                        tenantId,
                        OutboxEventTypes.ORDER_PAID,
                        "Order",
                        paid.getOrderId(),
                        payload,
                        "OrderPaid:" + paid.getOrderId());
            }
            settlementBillService.createSettlementOrder(payment, item);
        }
    }
}
