package com.quirogaotero.beans.inventory.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class LotStockKey implements Serializable {

  @Column(name = "lot_id", nullable = false)
  private UUID lotId;

  @Column(name = "location_id", nullable = false)
  private String locationId;

  protected LotStockKey() { }

  public LotStockKey(UUID lotId, String locationId) {
    this.lotId = lotId;
    this.locationId = locationId;
  }

  public UUID getLotId() { return lotId; }
  public String getLocationId() { return locationId; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LotStockKey other)) return false;
    return Objects.equals(lotId, other.lotId) && Objects.equals(locationId, other.locationId);
  }

  @Override
  public int hashCode() { return Objects.hash(lotId, locationId); }

}