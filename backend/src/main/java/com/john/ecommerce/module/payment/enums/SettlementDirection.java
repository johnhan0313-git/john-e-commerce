package com.john.ecommerce.module.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SettlementDirection {
    FORWARD("FORWARD"),
    REVERSE("REVERSE");

    private final String code;
}
