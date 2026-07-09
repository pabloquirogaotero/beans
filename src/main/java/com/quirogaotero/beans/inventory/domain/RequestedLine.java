package com.quirogaotero.beans.inventory.domain;

import java.util.Objects;

public record RequestedLine(SkuId skuId, Quantity quantity) {

  public RequestedLine {
    Objects.requireNonNull(skuId, "SkuId must not be null.");
    Objects.requireNonNull(quantity, "Quantity must not be null.");
  }

}
