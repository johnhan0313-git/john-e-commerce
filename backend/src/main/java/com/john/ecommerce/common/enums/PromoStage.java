package com.john.ecommerce.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PromoStage {
    ITEM("item"),
    COUPON("coupon"),
    ORDER("order"),
    FREIGHT("freight");

    private final String code;
}
