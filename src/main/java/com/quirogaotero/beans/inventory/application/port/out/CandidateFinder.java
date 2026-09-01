package com.quirogaotero.beans.inventory.application.port.out;

import com.quirogaotero.beans.inventory.domain.Candidate;
import com.quirogaotero.beans.inventory.domain.SkuId;

import java.util.List;
import java.util.Set;

public interface CandidateFinder {

  List<Candidate> findCandidates(Set<SkuId> skuIds);

}
