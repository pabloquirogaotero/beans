package com.quirogaotero.beans.inventory.application.service;

import com.quirogaotero.beans.inventory.application.port.in.ReservationResult;
import com.quirogaotero.beans.inventory.application.port.in.ReserveForCheckoutCommand;
import com.quirogaotero.beans.inventory.application.port.out.FakeCandidateFinder;
import com.quirogaotero.beans.inventory.application.port.out.InMemoryLotStockRepository;
import com.quirogaotero.beans.inventory.application.port.out.InMemoryReservationRepository;
import com.quirogaotero.beans.inventory.domain.*;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReserveForCheckoutServiceTest {

  private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T10:00:00Z"), ZoneOffset.UTC);
  private final Coordinates destination = new Coordinates(0, 0);
  private final Coordinates close = new Coordinates(1, 1);

  private final InMemoryLotStockRepository lotStocks = new InMemoryLotStockRepository();
  private final InMemoryReservationRepository reservations = new InMemoryReservationRepository();
  private final ConsolidatedAllocationStrategy allocationStrategy = new ConsolidatedAllocationStrategy();

  private Candidate candidate(int available, String bestBefore, LocationType locationType) {
    Lot lot = new Lot(LotId.newLotId(), new SkuId("COF-COLHU-WBN-250"), new LotCode("L00000001"),
            LocalDate.parse(bestBefore));
    LotStockId id = new LotStockId(new LocationId("WRH-OUR-01"), lot.getId());
    lotStocks.seed(new LotStock(id, Quantity.of(available), Quantity.of(0), 0L));
    return new Candidate(new LocationId("WRH-OUR-01"), locationType, close, lot, Quantity.of(available));
  }

  private ReserveForCheckoutCommand order(int qty) {
    return new ReserveForCheckoutCommand(CheckoutId.newCheckoutId(), destination,
            List.of(new RequestedLine(new SkuId("COF-COLHU-WBN-250"), Quantity.of(qty))));
  }

  @Test
  void reservesSuccessfullyAndReturnsAHold() {
    Candidate candidate = candidate(5, "2027-01-01", LocationType.WAREHOUSE);

    ReservationResult result = new ReserveForCheckoutService(new FakeCandidateFinder(List.of(candidate)), lotStocks,
            reservations, allocationStrategy, clock, Duration.ofMinutes(15), 30)
            .reserveForCheckout(order(2));

    assertTrue(result.reserved());
    assertEquals(1, result.reservationIds().size());
    assertEquals(1, reservations.saved.size());
    assertEquals(Instant.parse("2026-07-01T10:15:00Z"), result.expiresAt());
    LotStock lotStock = lotStocks.find(new LotStockId(new LocationId("WRH-OUR-01"),
            candidate.lot().getId())).orElseThrow();
    assertEquals(2, lotStock.getReserved().value());
  }

  @Test
  void returnsSoldOutWhenStockIsInsufficient() {
    Candidate candidate = candidate(1, "2027-01-01", LocationType.WAREHOUSE);

    ReservationResult result = new ReserveForCheckoutService(new FakeCandidateFinder(List.of(candidate)), lotStocks,
            reservations, allocationStrategy, clock, Duration.ofMinutes(15), 30)
            .reserveForCheckout(order(5));

    assertFalse(result.reserved());
    assertTrue(reservations.saved.isEmpty());
  }

  @Test
  void excludesLotsBelowTheShelfLifeThreshold() {
    Candidate candidate = candidate(5, "2026-01-02", LocationType.WAREHOUSE);

    ReservationResult result = new ReserveForCheckoutService(new FakeCandidateFinder(List.of(candidate)), lotStocks,
            reservations, allocationStrategy, clock, Duration.ofMinutes(15), 30)
            .reserveForCheckout(order(2));

    assertFalse(result.reserved());
    assertTrue(reservations.saved.isEmpty());
  }

  @Test
  void neverReservesFromAStore() {
    Candidate candidate = candidate(5, "2026-01-02", LocationType.STORE);

    ReservationResult result = new ReserveForCheckoutService(new FakeCandidateFinder(List.of(candidate)), lotStocks,
            reservations, allocationStrategy, clock, Duration.ofMinutes(15), 30)
            .reserveForCheckout(order(2));

    assertFalse(result.reserved());
  }

}
