package com.quirogaotero.beans.inventory.application.port.out;

import com.quirogaotero.beans.inventory.domain.Candidate;
import com.quirogaotero.beans.inventory.domain.SkuId;

import java.util.List;
import java.util.Set;

public class FakeCandidateFinder implements CandidateFinder {

  private final List<Candidate> candidates;

  public FakeCandidateFinder(List<Candidate> candidates) {
    this.candidates = candidates;
  }

  @Override
  public List<Candidate> findCandidates(Set<SkuId> skuIds) {
    return candidates.stream().filter(c -> skuIds.contains(c.lot().getSkuId())).toList();
  }

}
