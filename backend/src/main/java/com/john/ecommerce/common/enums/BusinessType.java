package com.john.ecommerce.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BusinessType {
    B2C("b2c", "零售商城"),
    B2B("b2b", "批发采购"),
    B2B2C("b2b2c", "多商家平台"),
    GROUP_BUY("group_buy", "社区团购"),
    DINING("dining", "点餐/到店"),
    DISTRIBUTION("distribution", "分销商城");

    private final String code;
    private final String label;
}
