package com.quirogaotero.beans.ordering.domain;

import com.quirogaotero.beans.inventory.domain.CheckoutId;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Something that happened: a paid checkout became an order. Carries its own {@code eventId}
 * so a consumer that receives it twice can recognise the duplicate.
 */
public record OrderPlaced(UUID eventId, OrderId orderId, CheckoutId checkoutId,
                          List<OrderLine> lines, Instant occurredAt) {

    public OrderPlaced {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(checkoutId, "checkoutId");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(lines, "lines");
        if (lines.isEmpty())
            throw new IllegalArgumentException("An OrderPlaced event must have at least one line.");
        lines = List.copyOf(lines);
    }

    public static OrderPlaced from(Order order) {
        return new OrderPlaced(UUID.randomUUID(), order.getId(), order.getCheckoutId(),
                order.getLines(), order.getPlacedAt());
    }

}
