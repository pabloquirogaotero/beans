package com.quirogaotero.beans.inventory.domain;

public record LotCode(String value) {

  public LotCode {
    if (value == null || value.isBlank())
      throw new IllegalArgumentException("LotCode must not be blank.");
  }

}
