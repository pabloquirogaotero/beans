package com.quirogaotero.beans.inventory.domain;

import java.util.List;
import java.util.Optional;

public interface AllocationStrategy {

  Optional<AllocationPlan> allocate(List<RequestedLine> lines, Coordinates destination, List<Candidate> candidates);

}
