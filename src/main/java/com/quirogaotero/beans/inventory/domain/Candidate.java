package com.quirogaotero.beans.inventory.domain;

import java.util.Objects;

public record Candidate(LocationId locationId, LocationType locationType, Coordinates locationCoordinates,
                        Lot lot, Quantity available) {

  public Candidate {
    Objects.requireNonNull(locationId, "LocationId must not be null.");
    Objects.requireNonNull(locationType, "LocationType must not be null.");
    Objects.requireNonNull(locationCoordinates, "LocationCoordinates must not be null.");
    Objects.requireNonNull(lot, "Lot must not be null.");
    Objects.requireNonNull(available, "Available must not be null.");
  }

}
