package com.john.ecommerce.module.payment.channel.route;

import com.john.ecommerce.module.payment.channel.PayChannel;
import com.john.ecommerce.module.payment.entity.PayChannelConfig;
import lombok.Data;

@Data
public class RouteResult {
    private PayChannelConfig channelConfig;
    private PayChannel payChannel;
}
