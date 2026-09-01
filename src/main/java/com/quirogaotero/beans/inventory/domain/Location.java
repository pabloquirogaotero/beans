package com.quirogaotero.beans.inventory.domain;

import java.util.Objects;

public class Location {

  private final LocationId id;
  private final LocationType type;
  private final Coordinates coordinates;

  public Location(LocationId id, LocationType type, Coordinates coordinates) {
    this.id = Objects.requireNonNull(id, "Id must not be null.");
    this.type = Objects.requireNonNull(type, "Type must not be null.");
    this.coordinates = Objects.requireNonNull(coordinates, "Coordinates must not be null.");
  }

  public boolean isStore() { return this.type == LocationType.STORE; }
  public boolean isWarehouse() { return this.type == LocationType.WAREHOUSE; }

  public LocationId getId() { return this.id; }
  public LocationType getType() { return this.type; }
  public Coordinates getCoordinates() { return this.coordinates; }

}
