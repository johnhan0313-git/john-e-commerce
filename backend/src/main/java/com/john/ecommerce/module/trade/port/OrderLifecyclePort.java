package com.john.ecommerce.module.trade.port;

import com.john.ecommerce.module.trade.port.command.*;

/**
 * Trade-owned order lifecycle. Other bounded contexts must mutate order status only through this port.
 */
public interface OrderLifecyclePort {

    /**
     * Record a payment allocation. When remaining payable reaches zero and status is PENDING, transitions to PAID.
     * Idempotent when already PAID with matching payNo.
     * Inventory consume is not done here — callers append {@code OrderPaid} outbox when
     * {@link MarkPaidResult#isPublishOrderPaid()} is true.
     */
    MarkPaidResult markPaid(MarkPaidCommand cmd);

    /** Transition to REFUNDING; snapshots prior status for reject/partial restore. */
    void startRefund(StartRefundCommand cmd);

    /** Full refund succeeded: REFUNDING → REFUNDED. */
    void completeRefund(CompleteRefundCommand cmd);

    /**
     * Partial refund succeeded or refund rejected: restore status from snapshot (or provided restoreTo).
     */
    void restoreAfterRefund(RestoreAfterRefundCommand cmd);

    void markShipped(ShipCommand cmd);

    void markDelivered(DeliverCommand cmd);

    void markCompleted(CompleteCommand cmd);

    void cancel(CancelCommand cmd);
}
