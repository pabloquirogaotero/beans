package com.quirogaotero.beans.inventory.domain;

import java.util.Objects;
import java.util.UUID;

public record LotId(UUID value) {

  public LotId {
    Objects.requireNonNull(value, "LotId must not be null.");
  }

  public static LotId newLotId() {
    return new LotId(UUID.randomUUID());
  }

}
