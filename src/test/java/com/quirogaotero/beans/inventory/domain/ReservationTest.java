package com.quirogaotero.beans.inventory.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReservationTest {

  private final Instant now = Instant.parse("2026-07-01T10:00:00Z");
  private final LotStockId lotStockId = new LotStockId(new LocationId("WRH-OUR-01"), LotId.newLotId());

  @Test
  void startsActive() {
    Reservation reservation = Reservation.newActiveReservation(CheckoutId.newCheckoutId(), lotStockId,
            Quantity.of(2), now.plusSeconds(900));
    assertEquals(ReservationStatus.ACTIVE, reservation.getStatus());
  }

  @Test
  void confirmBeforeExpiryMovesToConfirmed() {
    Reservation reservation = Reservation.newActiveReservation(CheckoutId.newCheckoutId(), lotStockId,
            Quantity.of(2), now.plusSeconds(900));
    reservation.confirm(now);
    assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
  }

  @Test
  void confirmAfterExpiryThrows() {
    Reservation reservation = Reservation.newActiveReservation(CheckoutId.newCheckoutId(), lotStockId,
            Quantity.of(2), now.minusSeconds(1));
    assertThrows(IllegalStateException.class, () -> reservation.confirm(now));
  }

  @Test
  void releaseMovesToReleased() {
    Reservation reservation = Reservation.newActiveReservation(CheckoutId.newCheckoutId(), lotStockId,
            Quantity.of(2), now.plusSeconds(900));
    reservation.release();
    assertEquals(ReservationStatus.RELEASED, reservation.getStatus());
  }

  @Test
  void cannotConfirmAReleasedReservation() {
    Reservation reservation = Reservation.newActiveReservation(CheckoutId.newCheckoutId(), lotStockId,
            Quantity.of(2), now.plusSeconds(900));
    reservation.release();
    assertThrows(IllegalStateException.class, () -> reservation.confirm(now));
  }

  @Test
  void cannotReleaseAConfirmedReservation() {
    Reservation reservation = Reservation.newActiveReservation(CheckoutId.newCheckoutId(), lotStockId,
            Quantity.of(2), now.plusSeconds(900));
    reservation.confirm(now);
    assertThrows(IllegalStateException.class, reservation::release);
  }

}
