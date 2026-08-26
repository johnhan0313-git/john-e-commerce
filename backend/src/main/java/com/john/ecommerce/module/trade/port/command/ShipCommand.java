package com.john.ecommerce.module.trade.port.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShipCommand {
    Long orderId;
    /** true when all order lines are shipped. */
    boolean allShipped;
}
