package com.quirogaotero.beans.ordering.domain;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CheckoutTest {

    private final List<OrderLine> lines = List.of(new OrderLine("COF-COLHU-WBN-250", 2));
    private final Money total = Money.euros(2500);

    @Test
    void a_started_checkout_has_started_status() {
        var checkout = Checkout.start(lines, total, Instant.now());
        assertEquals(CheckoutStatus.STARTED, checkout.getStatus());
        assertEquals(total, checkout.getTotal());
    }

    @Test
    void a_checkout_must_have_at_least_one_line() {
        assertThrows(IllegalArgumentException.class,
                () -> Checkout.start(List.of(), total, Instant.now()));
    }

    @Test
    void a_started_checkout_can_be_paid() {
        var checkout = Checkout.start(lines, total, Instant.now());
        checkout.markPaid();
        assertEquals(CheckoutStatus.PAID, checkout.getStatus());
    }

    @Test
    void cannot_pay_an_already_paid_checkout() {
        var checkout = Checkout.start(lines, total, Instant.now());
        checkout.markPaid();
        assertThrows(IllegalStateException.class, checkout::markPaid);
    }

}