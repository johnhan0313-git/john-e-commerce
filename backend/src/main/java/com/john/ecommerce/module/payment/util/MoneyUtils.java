package com.john.ecommerce.module.payment.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {

    private MoneyUtils() {}

    public static long toCents(BigDecimal amount) {
        if (amount == null) {
            return 0L;
        }
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }

    public static BigDecimal toDecimal(long cents) {
        return BigDecimal.valueOf(cents, 2);
    }
}
