package com.john.ecommerce.module.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RouteStrategyType {
    PRIORITY("PRIORITY"),
    WEIGHT_LB("WEIGHT_LB"),
    FAIL_FAST("FAIL_FAST");

    private final String code;

    public static RouteStrategyType of(String code) {
        for (RouteStrategyType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        return PRIORITY;
    }
}
