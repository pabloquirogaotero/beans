package com.quirogaotero.beans.inventory.domain;

import java.time.LocalDate;
import java.util.Objects;

public class Lot {

  private final LotId id;
  private final SkuId skuId;
  private final LotCode lotCode;
  private final LocalDate bestBefore;

  public Lot(LotId id, SkuId skuId, LotCode lotCode, LocalDate bestBefore) {
    this.id = Objects.requireNonNull(id, "Id must not be null.");
    this.skuId = Objects.requireNonNull(skuId, "SkuId must not be null.");
    this.lotCode = Objects.requireNonNull(lotCode, "LotCode must not be null.");
    this.bestBefore = Objects.requireNonNull(bestBefore, "BestBefore must not be null.");
  }

  public boolean isExpired(LocalDate on) { return on.isAfter(this.bestBefore); }

  public LotId getId() { return this.id; }
  public SkuId getSkuId() { return this.skuId; }
  public LotCode getLotCode() { return this.lotCode; }
  public LocalDate getBestBefore() { return this.bestBefore; }

}
