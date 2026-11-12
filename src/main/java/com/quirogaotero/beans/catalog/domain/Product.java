package com.quirogaotero.beans.catalog.domain;

public record Product(String sku, String name, long priceInCents, String currency) {

  public Product {
    if (sku == null || sku.isBlank())
      throw new IllegalArgumentException("Product sku must not be blank.");
    if (name == null || name.isBlank())
      throw new IllegalArgumentException("Product name must not be blank.");
    if (priceInCents <= 0)
      throw new IllegalArgumentException("Product price must be positive.");
    if (currency == null || currency.isBlank())
      throw new IllegalArgumentException("Product currency must not be blank.");
  }

}
