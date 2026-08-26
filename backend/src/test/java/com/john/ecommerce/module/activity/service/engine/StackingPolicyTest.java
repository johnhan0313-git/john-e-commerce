package com.john.ecommerce.module.activity.service.engine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StackingPolicyTest {

    private final StackingPolicy policy = new StackingPolicy();

    @Test
    void higherPriorityWinsWithinSameNonStackableGroup() {
        PromoCandidate low = candidate("A", false, 10, "5");
        PromoCandidate high = candidate("A", false, 100, "20");
        List<PromoCandidate> selected = policy.select(List.of(low, high));
        assertThat(selected).containsExactly(high);
    }

    @Test
    void stackableSameGroupBothSelected() {
        PromoCandidate a = candidate("COUPON", true, 10, "5");
        PromoCandidate b = candidate("COUPON", true, 5, "3");
        assertThat(policy.select(List.of(a, b))).containsExactly(a, b);
    }

    @Test
    void differentGroupsCanCoexist() {
        PromoCandidate full = candidate("FULL", false, 50, "10");
        PromoCandidate coupon = candidate("COUPON", false, 40, "5");
        assertThat(policy.select(List.of(full, coupon))).containsExactly(full, coupon);
    }

    private static PromoCandidate candidate(String group, boolean stackable, int priority, String discount) {
        PromoCandidate c = new PromoCandidate();
        c.setStackGroup(group);
        c.setStackable(stackable);
        c.setPriority(priority);
        c.setDiscountAmount(Long.parseLong(discount));
        c.setActivityType("TEST");
        return c;
    }
}
