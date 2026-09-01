package com.quirogaotero.beans.inventory.domain;

public record Coordinates(double lat, double lon) {

  public double distanceTo(Coordinates other) {
    double dLat = Math.toRadians(other.lat - this.lat);
    double dLon = Math.toRadians(other.lon - this.lon);
    double thisLat = Math.toRadians(this.lat);
    double otherLat = Math.toRadians(other.lat);

    return 2 * 6371 * Math.asin(Math.sqrt(Math.pow(Math.sin(dLat / 2), 2)
            + Math.cos(thisLat) * Math.cos(otherLat)
            * Math.pow(Math.sin(dLon / 2), 2)));
  }

}
