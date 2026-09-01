package com.quirogaotero.beans.inventory.adapter.out.persistence;

import com.quirogaotero.beans.inventory.application.port.out.CandidateFinder;
import com.quirogaotero.beans.inventory.domain.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class CandidateFinderAdapter implements CandidateFinder {

  private final CandidateJpaRepository jpa;

  public CandidateFinderAdapter(CandidateJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public List<Candidate> findCandidates(Set<SkuId> skus) {
    Set<String> skuIds = skus.stream().map(SkuId::value).collect(Collectors.toSet());
    return jpa.findCandidateRows(skuIds).stream()
            .map(this::toCandidate)
            .toList();
  }

  private Candidate toCandidate(CandidateRow row) {
    var lot = new Lot(
            new LotId(row.getLotId()),
            new SkuId(row.getSkuId()),
            new LotCode(row.getLotCode()),
            row.getBestBefore());
    return new Candidate(
            new LocationId(row.getLocationId()),
            LocationType.valueOf(row.getLocationType()),
            new Coordinates(row.getLatitude(), row.getLongitude()),
            lot,
            Quantity.of(row.getAvailable()));
  }

}
