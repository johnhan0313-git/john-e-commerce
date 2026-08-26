package com.john.ecommerce.module.trade.port.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of {@code markPaid}: whether an OrderPaid outbox event should be appended for inventory consume.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkPaidResult {

    private boolean publishOrderPaid;
    private Long orderId;
    private Long warehouseId;
    private Long tenantId;

    public static MarkPaidResult none() {
        return MarkPaidResult.builder().publishOrderPaid(false).build();
    }
}
