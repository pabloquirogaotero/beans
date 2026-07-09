package com.quirogaotero.beans.inventory.domain;

public record Coordinates(double lat, double lon) {

  public double distanceTo(Coordinates other) {
    return 0.0;
  }

}
