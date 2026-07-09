package com.quirogaotero.beans.inventory.domain;

public record SkuId(String value) {

  public SkuId {
    if (value == null || value.isBlank())
      throw new IllegalArgumentException("SkuId must not be blank.");
  }

}
