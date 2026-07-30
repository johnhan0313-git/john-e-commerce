package com.john.ecommerce.module.payment.channel.route;

import com.john.ecommerce.module.payment.channel.PayChannel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PayChannelRegistry {

    private final Map<String, PayChannel> channelMap;

    public PayChannelRegistry(List<PayChannel> channels) {
        // key by the first supported channel type (lowercase)
        this.channelMap = channels.stream()
                .collect(Collectors.toMap(
                        c -> detectType(c).toUpperCase(),
                        Function.identity(),
                        (a, b) -> a));
    }

    public PayChannel get(String channelType) {
        return channelMap.get(channelType.toUpperCase());
    }

    private String detectType(PayChannel c) {
        for (String t : List.of("MOCK", "BALANCE", "WECHAT", "ALIPAY")) {
            if (c.supports(t)) return t;
        }
        return "UNKNOWN";
    }
}
