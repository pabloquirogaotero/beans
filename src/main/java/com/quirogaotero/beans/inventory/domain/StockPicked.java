package com.quirogaotero.beans.inventory.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Something that happened: the stock reserved for an order has been picked. The order is
 * known here only by its id; it belongs to another context.
 */
public record StockPicked(UUID eventId, UUID orderId, CheckoutId checkoutId,
                          List<Line> lines, Instant occurredAt) {

  public record Line(LotStockId lotStockId, Quantity quantity) {

    public Line {
      Objects.requireNonNull(lotStockId, "LotStockId must not be null.");
      Objects.requireNonNull(quantity, "Quantity must not be null.");
    }

  }

  public StockPicked {
    Objects.requireNonNull(eventId, "EventId must not be null.");
    Objects.requireNonNull(orderId, "OrderId must not be null.");
    Objects.requireNonNull(checkoutId, "CheckoutId must not be null.");
    Objects.requireNonNull(occurredAt, "OccurredAt must not be null.");
    if (lines == null || lines.isEmpty())
      throw new IllegalArgumentException("A StockPicked event must have at least one line.");
    lines = List.copyOf(lines);
  }

}
