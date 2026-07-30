package com.john.ecommerce.module.payment.channel.route;

import com.john.ecommerce.module.payment.entity.PayChannelConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChannelRouterWeightedSelectTest {

    @Test
    void picksFirstBucketWhenRandomInRange() {
        PayChannelConfig a = config("A", 70);
        PayChannelConfig b = config("B", 30);
        assertThat(ChannelRouter.weightedSelect(List.of(a, b), 0).getChannelType()).isEqualTo("A");
        assertThat(ChannelRouter.weightedSelect(List.of(a, b), 69).getChannelType()).isEqualTo("A");
        assertThat(ChannelRouter.weightedSelect(List.of(a, b), 70).getChannelType()).isEqualTo("B");
        assertThat(ChannelRouter.weightedSelect(List.of(a, b), 99).getChannelType()).isEqualTo("B");
    }

    private static PayChannelConfig config(String type, int weight) {
        PayChannelConfig c = new PayChannelConfig();
        c.setChannelType(type);
        c.setWeight(weight);
        return c;
    }
}
