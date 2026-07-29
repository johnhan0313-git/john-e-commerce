package com.john.ecommerce.module.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatus {
    PENDING(0, "待支付"),
    PROCESSING(1, "支付中"),
    SUCCESS(2, "支付成功"),
    FAILED(3, "支付失败"),
    CLOSED(4, "已关闭");

    private final int code;
    private final String label;

    public static PaymentStatus of(int code) {
        for (PaymentStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        throw new IllegalArgumentException("unknown payment status: " + code);
    }
}
