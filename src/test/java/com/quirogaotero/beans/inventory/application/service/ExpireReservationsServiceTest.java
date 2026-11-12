package com.quirogaotero.beans.inventory.application.service;

import com.quirogaotero.beans.inventory.application.port.out.InMemoryLotStockRepository;
import com.quirogaotero.beans.inventory.application.port.out.InMemoryReservationRepository;
import com.quirogaotero.beans.inventory.application.port.out.InMemoryStockLedger;
import com.quirogaotero.beans.inventory.domain.*;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

class ExpireReservationsServiceTest {

  private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T10:00:00Z"), ZoneOffset.UTC);
  private final InMemoryReservationRepository reservations = new InMemoryReservationRepository();
  private final InMemoryLotStockRepository lotStocks = new InMemoryLotStockRepository();
  private final InMemoryStockLedger ledger = new InMemoryStockLedger();
  private final ExpireReservationsService service =
          new ExpireReservationsService(reservations, lotStocks, ledger, clock);

  private final LotStockId lotStockId = new LotStockId(new LocationId("WRH-OUR-01"), LotId.newLotId());

  private Reservation holdOf(int quantity, Instant expiresAt) {
    Reservation reservation = Reservation.newActiveReservation(
            CheckoutId.newCheckoutId(), lotStockId, Quantity.of(quantity), expiresAt);
    reservations.save(reservation);
    return reservation;
  }

  @Test
  void releasesExpiredHoldsAndRecordsTheReleaseInTheLedger() {
    lotStocks.seed(new LotStock(lotStockId, Quantity.of(10), Quantity.of(3), 0L));
    Reservation expired = holdOf(3, clock.instant().minusSeconds(1));

    int released = service.expireReservations();

    assertEquals(1, released);
    assertEquals(0, lotStocks.find(lotStockId).orElseThrow().getReserved().value());
    assertEquals(ReservationStatus.RELEASED, expired.getStatus());
    assertEquals(1, ledger.entries.size());
    StockLedgerEntry entry = ledger.entries.getFirst();
    assertEquals(StockMovementType.RELEASE, entry.type());
    assertEquals(Quantity.of(3), entry.quantity());
    assertEquals(expired.getCheckoutId(), entry.checkoutId());
    assertEquals(clock.instant(), entry.occurredAt());
  }

  @Test
  void leavesHoldsThatHaveNotExpiredAlone() {
    lotStocks.seed(new LotStock(lotStockId, Quantity.of(10), Quantity.of(3), 0L));
    holdOf(3, clock.instant().plusSeconds(60));

    assertEquals(0, service.expireReservations());
    assertEquals(3, lotStocks.find(lotStockId).orElseThrow().getReserved().value());
    assertTrue(ledger.entries.isEmpty());
  }

}
