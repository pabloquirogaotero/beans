package com.quirogaotero.beans.ordering.domain;

import com.quirogaotero.beans.inventory.domain.CheckoutId;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private final CheckoutId checkout = CheckoutId.newCheckoutId();
    private final List<OrderLine> lines = List.of(new OrderLine("COF-COLHU-WBN-250", 2));

    @Test
    void a_placed_order_starts_confirmed() {
        var order = Order.place(checkout, lines, Instant.now());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(1, order.getLines().size());
    }

    @Test
    void an_order_must_have_at_least_one_line() {
        assertThrows(IllegalArgumentException.class,
            () -> Order.place(checkout, List.of(), Instant.now()));
    }

    @Test
    void confirmed_order_can_be_fulfilled() {
        var order = Order.place(checkout, lines, Instant.now());
        order.markFulfilled();
        assertEquals(OrderStatus.FULFILLED, order.getStatus());
    }

    @Test
    void cannot_fulfil_an_already_fulfilled_order() {
        var order = Order.place(checkout, lines, Instant.now());
        order.markFulfilled();
        assertThrows(IllegalStateException.class, order::markFulfilled);
    }

    @Test
    void order_lines_are_immutable() {
        var order = Order.place(checkout, lines, Instant.now());
        assertThrows(UnsupportedOperationException.class,
            () -> order.getLines().add(new OrderLine("X", 1)));
    }

}
