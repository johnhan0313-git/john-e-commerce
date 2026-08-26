package com.john.ecommerce.module.trade.port.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DeliverCommand {
    Long orderId;
}
