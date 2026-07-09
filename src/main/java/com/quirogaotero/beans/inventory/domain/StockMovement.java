package com.quirogaotero.beans.inventory.domain;

import java.time.Instant;
import java.util.UUID;

public record StockMovement(UUID id, LotStockId lotStockId, StockMovementType type,
                            Quantity quantity, Instant occurredAt) {

  public static StockMovement of(LotStockId lotStockId, StockMovementType type, Quantity quantity) {
    return new StockMovement(UUID.randomUUID(), lotStockId, type, quantity, Instant.now());
  }

}
