package com.quirogaotero.beans.inventory.domain;

import java.util.Objects;

public class LotStock {

  private final LotStockId id;
  private Quantity onHand;
  private Quantity reserved;

  public LotStock(LotStockId id, Quantity onHand, Quantity reserved) {
    this.id = Objects.requireNonNull(id, "Id must not be null.");
    this.onHand = Objects.requireNonNull(onHand, "OnHand must not be null.");
    this.reserved = Objects.requireNonNull(reserved, "Reserved must not be null.");

    if (this.reserved.isGreaterThan(this.onHand))
      throw new IllegalStateException("Invariant violated: reserved > onHand for " + id);
  }

  public static LotStock newEmptyLotStock(LotStockId id) {
    return new LotStock(id, Quantity.ZERO, Quantity.ZERO);
  }

  public Quantity getAvailable() {
    return this.onHand.minus(this.reserved);
  }

  public StockMovement reserve(Quantity qty) {
    if (this.reserved.plus(qty).isGreaterThan(this.onHand))
      throw new InsufficientStockException(this.id, qty, this.getAvailable());

    this.reserved = reserved.plus(qty);
    return StockMovement.of(this.id, StockMovementType.RESERVE, qty);
  }

  public StockMovement release(Quantity qty) {
    this.reserved = this.reserved.minus(qty);
    return StockMovement.of(this.id, StockMovementType.RELEASE, qty);
  }

  public LotStockId getId() { return this.id; }
  public Quantity getOnHand() { return this.onHand; }
  public Quantity getReserved() { return this.reserved; }

}
