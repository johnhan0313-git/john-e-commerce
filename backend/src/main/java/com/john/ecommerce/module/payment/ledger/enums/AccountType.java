package com.john.ecommerce.module.payment.ledger.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountType {
    PLATFORM("PLATFORM"),
    USER_BALANCE("USER_BALANCE"),
    MERCHANT_BALANCE("MERCHANT_BALANCE"),
    SHOP_BALANCE("SHOP_BALANCE"),
    MARKETING("MARKETING"),
    DEPOSIT("DEPOSIT"),
    CREDIT("CREDIT"),
    DISTRIBUTOR("DISTRIBUTOR"),
    GIFT_CARD("GIFT_CARD");

    private final String code;
}
