package com.john.ecommerce.module.trade.port.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RestoreAfterRefundCommand {
    Long orderId;
    /** Optional explicit restore target; when null, uses statusBeforeRefund snapshot. */
    Integer restoreToStatus;
}
