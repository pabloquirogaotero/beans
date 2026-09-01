package com.quirogaotero.beans.inventory.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConsolidatedAllocationStrategyTest {

  private final AllocationStrategy strategy = new ConsolidatedAllocationStrategy();

  private final Coordinates destination = new Coordinates(0, 0);
  private final Coordinates close = new Coordinates(1, 1);
  private final Coordinates far = new Coordinates(2, 2);

  private int qtyFrom(AllocationPlan plan, String locationId) {
    return plan.allocations().stream()
            .filter(a -> a.lotStockId().locationId().value().equals(locationId))
            .mapToInt(a -> a.quantity().value()).sum();
  }

  @Test
  void singleLocationCoveringAllIsChosenClosestFirst() {
    List<Candidate> candidates = List.of(
        new Candidate(new LocationId("WRH-CLS"), LocationType.WAREHOUSE, close,
              new Lot(LotId.newLotId(), new SkuId("COF-COLHU-WBN-250"), new LotCode("L00000001"),
                      LocalDate.parse("2027-01-01")), Quantity.of(10)),
      new Candidate(new LocationId("WRH-FAR"), LocationType.WAREHOUSE, far,
              new Lot(LotId.newLotId(), new SkuId("COF-COLHU-WBN-250"), new LotCode("L00000001"),
                      LocalDate.parse("2027-01-01")), Quantity.of(10)));

    Optional<AllocationPlan> plan = strategy.allocate(
            List.of(new RequestedLine(new SkuId("COF-COLHU-WBN-250"),
            Quantity.of(2))), destination, candidates);

    assertTrue(plan.isPresent());
    assertTrue(plan.get().allocations().stream()
            .allMatch(a -> a.lotStockId().locationId().value().equals("WRH-CLS")));
  }

  @Test
  void closerButIncompleteLocationIsSkippedForAFartherCompleteOne() {
    List<Candidate> candidates = List.of(
            new Candidate(new LocationId("WRH-CLS"), LocationType.WAREHOUSE, close,
                    new Lot(LotId.newLotId(), new SkuId("COF-COLHU-WBN-250"), new LotCode("L00000001"),
                            LocalDate.parse("2027-01-01")), Quantity.of(2)),
            new Candidate(new LocationId("WRH-FAR"), LocationType.WAREHOUSE, far,
                    new Lot(LotId.newLotId(), new SkuId("COF-COLHU-WBN-250"), new LotCode("L00000001"),
                            LocalDate.parse("2027-01-01")), Quantity.of(10)));

    Optional<AllocationPlan> plan = strategy.allocate(
            List.of(new RequestedLine(new SkuId("COF-COLHU-WBN-250"),
                    Quantity.of(5))), destination, candidates);

    assertTrue(plan.isPresent());
    assertTrue(plan.get().allocations().stream()
            .allMatch(a -> a.lotStockId().locationId().value().equals("WRH-FAR")));
  }

  @Test
  void splitsAcrossLocationsClosestFirstWhenNoSingleOneCovers() {
    List<Candidate> candidates = List.of(
            new Candidate(new LocationId("WRH-FAR"), LocationType.WAREHOUSE, far,
                    new Lot(LotId.newLotId(), new SkuId("COF-COLHU-WBN-250"), new LotCode("L00000001"),
                            LocalDate.parse("2027-01-01")), Quantity.of(5)),
            new Candidate(new LocationId("WRH-CLS"), LocationType.WAREHOUSE, close,
                    new Lot(LotId.newLotId(), new SkuId("COF-COLHU-WBN-250"), new LotCode("L00000001"),
                            LocalDate.parse("2027-01-01")), Quantity.of(5)));

    Optional<AllocationPlan> plan = strategy.allocate(
            List.of(new RequestedLine(new SkuId("COF-COLHU-WBN-250"),
                    Quantity.of(8))), destination, candidates);

    assertTrue(plan.isPresent());
    assertEquals(5, qtyFrom(plan.get(), "WRH-CLS"));
    assertEquals(3, qtyFrom(plan.get(), "WRH-FAR"));
  }

  @Test
  void consumesOldestLotFirstWithinALocation() {
    Lot older = new Lot(LotId.newLotId(), new SkuId("COF-COLHU-WBN-250"), new LotCode("L00000001"), LocalDate.parse("2027-01-01"));
    Lot newer = new Lot(LotId.newLotId(), new SkuId("COF-COLHU-WBN-250"), new LotCode("L00000002"), LocalDate.parse("2028-01-01"));

    List<Candidate> candidates = List.of(
            new Candidate(new LocationId("WRH-CLS"), LocationType.WAREHOUSE, close, newer, Quantity.of(5)),
            new Candidate(new LocationId("WRH-CLS"), LocationType.WAREHOUSE, close, older, Quantity.of(5)));

    Optional<AllocationPlan> plan = strategy.allocate(
            List.of(
                    new RequestedLine(new SkuId("COF-COLHU-WBN-250"), Quantity.of(2))
            ), destination, candidates);

    assertTrue(plan.isPresent());
    assertEquals(1, plan.get().allocations().size());
    assertEquals(older.getId(), plan.get().allocations().getFirst().lotStockId().lotId());
    assertEquals(2, plan.get().allocations().getFirst().quantity().value());
  }

  @Test
  void splitsOneLineAcrossLotsWhenOldestCannotCoverIt() {
    Lot older = new Lot(LotId.newLotId(), new SkuId("COF-COLHU-WBN-250"), new LotCode("L00000001"), LocalDate.parse("2027-01-01"));
    Lot newer = new Lot(LotId.newLotId(), new SkuId("COF-COLHU-WBN-250"), new LotCode("L00000002"), LocalDate.parse("2028-01-01"));

    List<Candidate> candidates = List.of(
            new Candidate(new LocationId("WRH-CLS"), LocationType.WAREHOUSE, close, newer, Quantity.of(5)),
            new Candidate(new LocationId("WRH-CLS"), LocationType.WAREHOUSE, close, older, Quantity.of(5)));

    Optional<AllocationPlan> plan = strategy.allocate(
            List.of(
                    new RequestedLine(new SkuId("COF-COLHU-WBN-250"), Quantity.of(8))
            ), destination, candidates);

    assertTrue(plan.isPresent());
    int fromOlder = plan.get().allocations().stream()
            .filter(a -> a.lotStockId().lotId().equals(older.getId()))
            .mapToInt(a -> a.quantity().value()).sum();
    int fromNewer = plan.get().allocations().stream()
            .filter(a -> a.lotStockId().lotId().equals(newer.getId()))
            .mapToInt(a -> a.quantity().value()).sum();
    assertEquals(5, fromOlder);
    assertEquals(3, fromNewer);
  }

  @Test
  void allocateNothingWhenOrderCannotBeFullyCovered() {
    List<Candidate> candidates = List.of(
            new Candidate(new LocationId("WRH-FAR"), LocationType.WAREHOUSE, far,
                    new Lot(LotId.newLotId(), new SkuId("COF-COLHU-WBN-250"), new LotCode("L00000001"),
                            LocalDate.parse("2027-01-01")), Quantity.of(5)),
            new Candidate(new LocationId("WRH-CLS"), LocationType.WAREHOUSE, close,
                    new Lot(LotId.newLotId(), new SkuId("COF-COLHU-WBN-250"), new LotCode("L00000001"),
                            LocalDate.parse("2027-01-01")), Quantity.of(5)));

    Optional<AllocationPlan> plan = strategy.allocate(
            List.of(
                    new RequestedLine(new SkuId("COF-COLHU-WBN-250"), Quantity.of(6)),
                    new RequestedLine(new SkuId("COF-COLHU-WBN-500"), Quantity.of(2))
            ), destination, candidates);

    assertTrue(plan.isEmpty());
  }

  @Test
  void storeStockIsNeverAllocatedToOnlineOrders() {
    List<Candidate> candidates = List.of(
            new Candidate(new LocationId("STR-OUR-01"), LocationType.STORE, close,
                    new Lot(LotId.newLotId(), new SkuId("COF-COLHU-WBN-250"), new LotCode("L00000001"),
                            LocalDate.parse("2027-01-01")), Quantity.of(5)));

    Optional<AllocationPlan> plan = strategy.allocate(
            List.of(
                    new RequestedLine(new SkuId("COF-COLHU-WBN-250"), Quantity.of(2))
            ), destination, candidates);

    assertTrue(plan.isEmpty());
  }

  @Test
  void multiSkuOrderCoveredByASingleLocation() {
    List<Candidate> candidates = List.of(
            new Candidate(new LocationId("WRH-FAR"), LocationType.WAREHOUSE, far,
                    new Lot(LotId.newLotId(), new SkuId("COF-COLHU-WBN-250"), new LotCode("L00000001"),
                            LocalDate.parse("2027-01-01")), Quantity.of(5)),
            new Candidate(new LocationId("WRH-CLS"), LocationType.WAREHOUSE, close,
                    new Lot(LotId.newLotId(), new SkuId("COF-COLHU-WBN-500"), new LotCode("L00000002"),
                            LocalDate.parse("2027-01-01")), Quantity.of(5)));

    Optional<AllocationPlan> plan = strategy.allocate(
            List.of(
                    new RequestedLine(new SkuId("COF-COLHU-WBN-250"), Quantity.of(2)),
                    new RequestedLine(new SkuId("COF-COLHU-WBN-500"), Quantity.of(2))
            ), destination, candidates);

    assertTrue(plan.isPresent());
    assertEquals(4, plan.get().allocations().stream().mapToInt(a -> a.quantity().value()).sum());
  }

}
