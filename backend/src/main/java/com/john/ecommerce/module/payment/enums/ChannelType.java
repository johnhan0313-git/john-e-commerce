package com.john.ecommerce.module.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChannelType {
    MOCK("MOCK"),
    BALANCE("BALANCE"),
    WECHAT("WECHAT"),
    ALIPAY("ALIPAY");

    private final String code;
}
