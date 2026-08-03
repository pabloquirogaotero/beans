package com.quirogaotero.beans.inventory.adapter.out.persistence;

import com.quirogaotero.beans.inventory.domain.*;

public final class LocationMapper {

  private LocationMapper() { }

  public static Location toDomain(LocationJpaEntity entity) {
    return new Location(
            new LocationId(entity.getId()),
            entity.getType(),
            new Coordinates(entity.getLatitude(), entity.getLongitude()));
  }

  public static LocationJpaEntity toEntity(Location location) {
    return new LocationJpaEntity(
            location.getId().value(),
            location.getType(),
            location.getCoordinates().lat(),
            location.getCoordinates().lon());
  }

}
