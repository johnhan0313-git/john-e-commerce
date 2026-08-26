package com.john.ecommerce.common.money;

import com.john.ecommerce.common.exception.BizException;

/**
 * Immutable monetary amount in minor units (cents for CNY).
 */
public final class Money {

    private final long cents;

    private Money(long cents) {
        if (cents < 0) {
            throw new BizException("金额不能为负");
        }
        this.cents = cents;
    }

    public static Money ofCents(long cents) {
        return new Money(cents);
    }

    public static Money zero() {
        return new Money(0L);
    }

    public long cents() {
        return cents;
    }

    public Money plus(Money other) {
        return ofCents(Math.addExact(this.cents, other.cents));
    }

    public Money minus(Money other) {
        return ofCents(Math.subtractExact(this.cents, other.cents));
    }

    public Money times(int qty) {
        if (qty < 0) throw new BizException("数量不能为负");
        return ofCents(Math.multiplyExact(this.cents, qty));
    }

    public boolean greaterThan(Money other) {
        return this.cents > other.cents;
    }

    public boolean greaterThanOrEqual(Money other) {
        return this.cents >= other.cents;
    }

    public int compareTo(Money other) {
        return Long.compare(this.cents, other.cents);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return cents == money.cents;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(cents);
    }

    @Override
    public String toString() {
        return Long.toString(cents);
    }
}
