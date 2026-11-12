package com.quirogaotero.beans.inventory.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One line of the stock ledger: a movement of one lot at one location, the checkout that
 * caused it and, for a pick, the order the units left in. Entries are only ever appended,
 * which is what makes it possible to answer "which orders received lot X?" after the fact.
 */
public record StockLedgerEntry(UUID id, LotStockId lotStockId, StockMovementType type, Quantity quantity,
                               CheckoutId checkoutId, UUID orderId, Instant occurredAt) {

  public StockLedgerEntry {
    Objects.requireNonNull(id, "Id must not be null.");
    Objects.requireNonNull(lotStockId, "LotStockId must not be null.");
    Objects.requireNonNull(type, "Type must not be null.");
    Objects.requireNonNull(quantity, "Quantity must not be null.");
    Objects.requireNonNull(checkoutId, "CheckoutId must not be null.");
    Objects.requireNonNull(occurredAt, "OccurredAt must not be null.");
    if (type == StockMovementType.PICK && orderId == null)
      throw new IllegalArgumentException("A pick must name the order it was picked for.");
  }

  public static StockLedgerEntry forCheckout(StockMovement movement, CheckoutId checkoutId, Instant occurredAt) {
    return new StockLedgerEntry(UUID.randomUUID(), movement.lotStockId(), movement.type(), movement.quantity(),
            checkoutId, null, occurredAt);
  }

  public static StockLedgerEntry forOrder(StockMovement movement, CheckoutId checkoutId, UUID orderId,
                                          Instant occurredAt) {
    return new StockLedgerEntry(UUID.randomUUID(), movement.lotStockId(), movement.type(), movement.quantity(),
            checkoutId, Objects.requireNonNull(orderId, "OrderId must not be null."), occurredAt);
  }

}
