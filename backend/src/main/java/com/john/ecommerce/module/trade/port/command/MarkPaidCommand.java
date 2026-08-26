package com.john.ecommerce.module.trade.port.command;

import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class MarkPaidCommand {
    Long orderId;
    Long amount;
    String payNo;
    Long paidAt;
}
