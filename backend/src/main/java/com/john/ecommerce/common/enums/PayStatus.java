package com.john.ecommerce.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayStatus {
    UNPAID(0, "未支付"),
    PARTIAL(1, "部分支付"),
    PAID(2, "已付清");

    private final int code;
    private final String label;
}
