package com.john.ecommerce.module.payment.ledger.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LedgerTxnType {
    CREDIT("CREDIT"),
    DEBIT("DEBIT"),
    FREEZE("FREEZE"),
    UNFREEZE("UNFREEZE"),
    FREEZE_DEBIT("FREEZE_DEBIT"),
    TRANSFER("TRANSFER");

    private final String code;
}
