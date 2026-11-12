package com.quirogaotero.beans.ordering.domain;

import com.quirogaotero.beans.inventory.domain.CheckoutId;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class OrderPlacedTest {

    private final Instant placedAt = Instant.parse("2026-07-01T10:00:00Z");
    private final Order order = Order.place(CheckoutId.newCheckoutId(),
            List.of(new OrderLine("COF-COLHU-WBN-250", 2)), placedAt);

    @Test
    void describes_the_order_it_was_raised_for() {
        var event = OrderPlaced.from(order);

        assertEquals(order.getId(), event.orderId());
        assertEquals(order.getCheckoutId(), event.checkoutId());
        assertEquals(order.getLines(), event.lines());
        assertEquals(placedAt, event.occurredAt());
    }

    @Test
    void every_event_gets_its_own_id() {
        assertNotEquals(OrderPlaced.from(order).eventId(), OrderPlaced.from(order).eventId());
    }

    @Test
    void an_event_must_have_at_least_one_line() {
        assertThrows(IllegalArgumentException.class,
            () -> new OrderPlaced(UUID.randomUUID(), order.getId(), order.getCheckoutId(), List.of(), placedAt));
    }

    @Test
    void event_lines_are_immutable() {
        var event = OrderPlaced.from(order);
        assertThrows(UnsupportedOperationException.class,
            () -> event.lines().add(new OrderLine("X", 1)));
    }

}
