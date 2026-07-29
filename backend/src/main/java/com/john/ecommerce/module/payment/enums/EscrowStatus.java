package com.john.ecommerce.module.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EscrowStatus {
    NONE("NONE"),
    FROZEN("FROZEN"),
    RELEASED("RELEASED");

    private final String code;
}
