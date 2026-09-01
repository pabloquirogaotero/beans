package com.quirogaotero.beans.inventory.application.port.in;

import com.quirogaotero.beans.inventory.domain.ReservationId;

import java.time.Instant;
import java.util.List;

public record ReservationResult(boolean reserved, List<ReservationId> reservationIds,
                                Instant expiresAt, String reason) {

  public static ReservationResult newSuccessfulReservation(List<ReservationId> reservationIds,
                                                           Instant expiresAt) {
    return new ReservationResult(true, reservationIds, expiresAt, null);
  }

  public static ReservationResult newFailedReservation(String reason) {
    return new ReservationResult(false, List.of(), null, reason);
  }

}
