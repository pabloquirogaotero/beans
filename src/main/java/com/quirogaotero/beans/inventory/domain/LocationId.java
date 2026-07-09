package com.quirogaotero.beans.inventory.domain;

public record LocationId(String value) {

  public LocationId {
    if (value == null || value.isBlank())
      throw new IllegalArgumentException("LocationId must not be blank.");
  }

}
