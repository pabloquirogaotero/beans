package com.quirogaotero.beans.inventory.domain;

import java.util.List;
import java.util.Objects;

public record AllocationPlan(List<LotAllocation> allocations) {

  public AllocationPlan {
    Objects.requireNonNull(allocations, "Allocations must not be null.");
  }

}
