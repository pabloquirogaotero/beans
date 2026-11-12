package com.quirogaotero.beans.ordering.adapter.out.messaging;

import com.quirogaotero.beans.ordering.domain.OrderPlaced;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** The wire format of an OrderPlaced event: what consumers of the topic can rely on. */
public record OrderPlacedJson(UUID eventId, UUID orderId, UUID checkoutId,
                              List<Line> lines, Instant occurredAt) {

    public record Line(String sku, int quantity) { }

    public static OrderPlacedJson from(OrderPlaced event) {
        List<Line> lines = event.lines().stream()
                .map(l -> new Line(l.sku(), l.quantity())).toList();
        return new OrderPlacedJson(event.eventId(), event.orderId().value(),
                event.checkoutId().value(), lines, event.occurredAt());
    }

}
