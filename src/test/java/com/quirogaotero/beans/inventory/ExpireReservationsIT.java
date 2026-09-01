package com.quirogaotero.beans.inventory;

import com.quirogaotero.beans.TestcontainersConfiguration;
import com.quirogaotero.beans.inventory.application.port.in.ExpireReservationsUseCase;
import com.quirogaotero.beans.inventory.application.port.out.LotStockRepository;
import com.quirogaotero.beans.inventory.application.port.out.ReservationRepository;
import com.quirogaotero.beans.inventory.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ExpireReservationsIT {

  @Autowired ExpireReservationsUseCase expireReservations;
  @Autowired LotStockRepository lotStocks;
  @Autowired ReservationRepository reservations;

  private final LotStockId lotStockId = new LotStockId(
          new LocationId("WRH-OUR-01"),
          new LotId(UUID.fromString("22222222-2222-2222-2222-222222222222")));

  @Test
  void releases_stock_and_marks_reservation_released_when_expired() {
    LotStock stock = lotStocks.find(lotStockId).orElseThrow();
    int reservedBefore = stock.getReserved().value();

    Quantity heldQty = Quantity.of(3);
    stock.reserve(heldQty);
    lotStocks.save(stock);

    Reservation expired = Reservation.newActiveReservation(
            CheckoutId.newCheckoutId(),
            lotStockId,
            heldQty,
            Instant.now().minusSeconds(60));
    reservations.save(expired);

    assertEquals(reservedBefore + 3,
            lotStocks.find(lotStockId).orElseThrow().getReserved().value());

    int released = expireReservations.expireReservations();

    assertTrue(released >= 1, "at least the expired reservation should be released");

    LotStock afterSweep = lotStocks.find(lotStockId).orElseThrow();
    assertEquals(reservedBefore, afterSweep.getReserved().value(),
            "reserved must return to its pre-hold level");

    Reservation reloaded = reservations.findById(expired.getId()).orElseThrow();
    assertEquals(ReservationStatus.RELEASED, reloaded.getStatus(),
            "the expired reservation must be marked RELEASED");
  }

  @Test
  void leaves_active_unexpired_reservations_untouched() {
    LotStock stock = lotStocks.find(lotStockId).orElseThrow();
    int reservedBefore = stock.getReserved().value();

    Quantity heldQty = Quantity.of(2);
    stock.reserve(heldQty);
    lotStocks.save(stock);

    Reservation stillValid = Reservation.newActiveReservation(
            CheckoutId.newCheckoutId(),
            lotStockId,
            heldQty,
            Instant.now().plusSeconds(600));

    reservations.save(stillValid);

    expireReservations.expireReservations();

    assertEquals(reservedBefore + 2,
            lotStocks.find(lotStockId).orElseThrow().getReserved().value(),
            "an unexpired hold must keep its stock reserved");
    Reservation reloaded = reservations.findById(stillValid.getId()).orElseThrow();
    assertEquals(ReservationStatus.ACTIVE, reloaded.getStatus(),
            "an unexpired reservation must stay ACTIVE");
  }
}