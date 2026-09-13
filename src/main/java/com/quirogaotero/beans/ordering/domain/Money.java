package com.quirogaotero.beans.ordering.domain;

public record Money(long amountInCents, String currency) {

    public Money {
        if (amountInCents < 0)
            throw new IllegalArgumentException("Money amount must not be negative.");
        if (currency == null || currency.isBlank())
            throw new IllegalArgumentException("Money currency must not be blank.");
    }

    public static Money euros(long cents) { return new Money(cents, "EUR"); }

}
