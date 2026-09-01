package com.quirogaotero.beans.inventory.domain;

import java.util.Objects;

public record LotStockId(LocationId locationId, LotId lotId) {

  public LotStockId {
    Objects.requireNonNull(locationId, "LocationId must not be null.");
    Objects.requireNonNull(locationId, "LotId must not be null.");
  }

}
