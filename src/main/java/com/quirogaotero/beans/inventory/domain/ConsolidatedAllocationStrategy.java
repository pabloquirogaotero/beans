package com.quirogaotero.beans.inventory.domain;

import java.util.*;
import java.util.stream.Collectors;

public class ConsolidatedAllocationStrategy implements AllocationStrategy {

  @Override
  public Optional<AllocationPlan> allocate(List<RequestedLine> lines, Coordinates destination,
                                           List<Candidate> candidates) {
    Map<LocationId, List<Candidate>> byLocation = candidates.stream()
            .filter(c -> c.locationType() == LocationType.WAREHOUSE)
            .collect(Collectors.groupingBy(Candidate::locationId));

    List<LocationId> byDistance = byLocation.keySet().stream()
            .sorted(Comparator.comparingDouble(id ->
                    destination.distanceTo(byLocation.get(id).getFirst().locationCoordinates())))
            .toList();

    Map<SkuId, Integer> required = requiredBySku(lines);

    for (LocationId id : byDistance) {
      List<Candidate> atLocation = byLocation.get(id);
      if (coversAll(required, atLocation)) {
        List<LotAllocation> allocations = consume(atLocation, new HashMap<>(required));
        return Optional.of(new AllocationPlan(allocations));
      }
    }

    Map<SkuId, Integer> remaining = new HashMap<>(required);
    List<LotAllocation> allocations = new ArrayList<>();
    for (LocationId id : byDistance) {
      if (isFilled(remaining)) break;
      allocations.addAll(consume(byLocation.get(id), remaining));
    }

    return isFilled(remaining) ? Optional.of(new AllocationPlan(allocations)) : Optional.empty();
  }

  private static Map<SkuId, Integer> requiredBySku(List<RequestedLine> lines) {
    Map<SkuId, Integer> need = new HashMap<>();
    for (RequestedLine line : lines) need.merge(line.skuId(), line.quantity().value(), Integer::sum);
    return need;
  }

  private static boolean coversAll(Map<SkuId, Integer> required, List<Candidate> atLocation) {
    Map<SkuId, Integer> available = atLocation.stream().collect(Collectors.groupingBy(
            c -> c.lot().getSkuId(), Collectors.summingInt(c -> c.available().value())));
    return required.entrySet().stream()
            .allMatch(e -> available.getOrDefault(e.getKey(), 0) >= e.getValue());
  }

  private static List<LotAllocation> consume(List<Candidate> pool, Map<SkuId, Integer> need) {
    List<LotAllocation> result = new ArrayList<>();
    Map<SkuId, List<Candidate>> bySku = pool.stream()
            .collect(Collectors.groupingBy(c -> c.lot().getSkuId()));

    for (SkuId sku : new ArrayList<>(need.keySet())) {
      int remaining = need.get(sku);
      if (remaining <= 0) continue;

      List<Candidate> lots = bySku.getOrDefault(sku, List.of()).stream()
              .sorted(Comparator.comparing(c -> c.lot().getBestBefore()))
              .toList();

      for (Candidate c : lots) {
        if (remaining == 0) break;
        int take = Math.min(remaining, c.available().value());
        if (take > 0) {
          result.add(new LotAllocation(
                  new LotStockId(c.locationId(), c.lot().getId()), new Quantity(take)));
          remaining -= take;
        }
      }
      need.put(sku, remaining);
    }
    return result;
  }

  private static boolean isFilled(Map<SkuId, Integer> need) {
    return need.values().stream().allMatch(v -> v <= 0);
  }

}
