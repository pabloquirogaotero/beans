package com.quirogaotero.beans.inventory.domain;

import java.util.Objects;
import java.util.UUID;

public record ReservationId(UUID value) {

  public ReservationId {
    Objects.requireNonNull(value, "ReservationId must not be null.");
  }

  public static ReservationId newReservationId() {
    return new ReservationId(UUID.randomUUID());
  }

}
