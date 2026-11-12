package com.quirogaotero.beans.inventory.domain;

/** What a LotStock just did. The service that asked for it adds the why and the when. */
public record StockMovement(LotStockId lotStockId, StockMovementType type, Quantity quantity) {

  public static StockMovement of(LotStockId lotStockId, StockMovementType type, Quantity quantity) {
    return new StockMovement(lotStockId, type, quantity);
  }

}
