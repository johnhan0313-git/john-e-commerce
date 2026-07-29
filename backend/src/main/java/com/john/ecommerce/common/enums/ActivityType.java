package com.john.ecommerce.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActivityType {
    COUPON("coupon", "优惠券"),
    GROUP_BUY("group_buy", "团购/拼团"),
    SECKILL("seckill", "秒杀"),
    FULL_REDUCTION("full_reduction", "满减"),
    DISTRIBUTION("distribution", "分销");

    private final String code;
    private final String label;
}
