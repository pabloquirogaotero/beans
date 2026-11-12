package com.quirogaotero.beans.ordering.domain;

public record Money(long amountInCents, String currency) {

    public Money {
        if (amountInCents < 0)
            throw new IllegalArgumentException("Money amount must not be negative.");
        if (currency == null || currency.isBlank())
            throw new IllegalArgumentException("Money currency must not be blank.");
    }

    public static Money euros(long cents) { return new Money(cents, "EUR"); }

    public Money plus(Money other) {
        if (!currency.equals(other.currency))
            throw new IllegalArgumentException("Cannot add " + other.currency + " to " + currency + ".");
        return new Money(Math.addExact(amountInCents, other.amountInCents), currency);
    }

    public Money times(int quantity) {
        if (quantity < 0)
            throw new IllegalArgumentException("Cannot multiply money by a negative quantity.");
        return new Money(Math.multiplyExact(amountInCents, quantity), currency);
    }

}
