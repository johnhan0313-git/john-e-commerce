package com.john.ecommerce.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    PENDING(0, "待支付"),
    PAID(1, "已支付"),
    SHIPPED(2, "已发货"),
    DELIVERED(3, "已送达"),
    COMPLETED(4, "已完成"),
    CANCELLED(5, "已取消"),
    REFUNDING(6, "退款中"),
    REFUNDED(7, "已退款");

    private final int code;
    private final String label;
}
