package com.john.ecommerce.module.payment.ledger.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LedgerDirection {
    IN("IN"),
    OUT("OUT");

    private final String code;
}
