package com.quirogaotero.beans.inventory.domain;

import java.util.Objects;

public record LotAllocation(LotStockId lotStockId, Quantity quantity) {

  public LotAllocation {
    Objects.requireNonNull(lotStockId, "LotStockId must not be null.");
    Objects.requireNonNull(quantity, "Quantity must not be null.");
  }

}
