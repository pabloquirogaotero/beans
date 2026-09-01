package com.quirogaotero.beans.inventory.application.service;

import com.quirogaotero.beans.inventory.application.port.in.ReservationResult;
import com.quirogaotero.beans.inventory.application.port.in.ReserveForCheckoutCommand;
import com.quirogaotero.beans.inventory.application.port.in.ReserveForCheckoutUseCase;
import com.quirogaotero.beans.inventory.application.port.out.CandidateFinder;
import com.quirogaotero.beans.inventory.application.port.out.LotStockRepository;
import com.quirogaotero.beans.inventory.application.port.out.ReservationRepository;
import com.quirogaotero.beans.inventory.domain.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ReserveForCheckoutService implements ReserveForCheckoutUseCase {

  private final CandidateFinder candidateFinder;
  private final LotStockRepository lotStocks;
  private final ReservationRepository reservations;
  private final AllocationStrategy allocationStrategy;
  private final Clock clock;
  private final Duration holdDuration;
  private final int minShelfLifeDays;

  public ReserveForCheckoutService(CandidateFinder candidateFinder, LotStockRepository lotStocks,
                                   ReservationRepository reservations, AllocationStrategy allocationStrategy,
                                   Clock clock, Duration holdDuration, int minShelfLifeDays) {
    this.candidateFinder = candidateFinder;
    this.lotStocks = lotStocks;
    this.reservations = reservations;
    this.allocationStrategy = allocationStrategy;
    this.clock = clock;
    this.holdDuration = holdDuration;
    this.minShelfLifeDays = minShelfLifeDays;
  }

  @Override
  @Transactional
  public ReservationResult reserveForCheckout(ReserveForCheckoutCommand command) {
    // 1. Gather candidate snapshots for the requested SkuIds.
    Set<SkuId> skus = command.lines().stream()
            .map(RequestedLine::skuId).collect(Collectors.toSet());
    List<Candidate> candidates = candidateFinder.findCandidates(skus);

    // 2. Drop lots that don't meet the shelf-life threshold.
    LocalDate today = LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
    LocalDate minBestBefore = today.plusDays(minShelfLifeDays);
    List<Candidate> sellable = candidates.stream()
            .filter(c -> !c.lot().getBestBefore().isBefore(minBestBefore))
            .toList();

    // 3. Let the domain decide the plan.
    Optional<AllocationPlan> plan =
            allocationStrategy.allocate(command.lines(), command.destination(), sellable);
    if (plan.isEmpty())
      return ReservationResult.newFailedReservation("No warehouse can fulfil the order.");

    // 4. Reserve each LotStock and store the reservations.
    Instant expiresAt = clock.instant().plus(holdDuration);
    List<ReservationId> ids = new ArrayList<>();
    for (LotAllocation allocation : plan.get().allocations()) {
      LotStock lotStock = lotStocks.find(allocation.lotStockId())
              .orElseThrow(() -> new IllegalStateException(
                      "Allocated LotStock not found: " + allocation.lotStockId()));
      lotStock.reserve(allocation.quantity()); // Throws and rolls back the transaction if there is no sufficient stock.
      Reservation reservation = Reservation.newActiveReservation(
              command.checkoutId(), allocation.lotStockId(), allocation.quantity(), expiresAt);
      lotStocks.save(lotStock);
      reservations.save(reservation);
      ids.add(reservation.getId());
    }

    return ReservationResult.newSuccessfulReservation(ids, expiresAt);
  }

}
