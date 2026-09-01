package com.quirogaotero.beans.inventory.adapter.out.persistence;

import com.quirogaotero.beans.inventory.domain.LocationType;
import jakarta.persistence.*;

@Entity
@Table(name = "locations")
public class LocationJpaEntity {

  @Id
  private String id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private LocationType type;

  @Column(nullable = false)
  private double latitude;

  @Column(nullable = false)
  private double longitude;

  protected LocationJpaEntity() { }

  public LocationJpaEntity(String id, LocationType type, double latitude, double longitude) {
    this.id = id;
    this.type = type;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public String getId() { return id; }
  public LocationType getType() { return type; }
  public double getLatitude() { return latitude; }
  public double getLongitude() { return longitude; }

}
