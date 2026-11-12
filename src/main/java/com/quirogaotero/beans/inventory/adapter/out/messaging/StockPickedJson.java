package com.quirogaotero.beans.inventory.adapter.out.messaging;

import com.quirogaotero.beans.inventory.domain.StockPicked;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** The wire format of a StockPicked event: what consumers of the topic can rely on. */
public record StockPickedJson(UUID eventId, UUID orderId, UUID checkoutId,
                              List<Line> lines, Instant occurredAt) {

  public record Line(UUID lotId, String locationId, int quantity) { }

  public static StockPickedJson from(StockPicked event) {
    List<Line> lines = event.lines().stream()
            .map(l -> new Line(l.lotStockId().lotId().value(), l.lotStockId().locationId().value(),
                    l.quantity().value()))
            .toList();
    return new StockPickedJson(event.eventId(), event.orderId(), event.checkoutId().value(),
            lines, event.occurredAt());
  }

}
