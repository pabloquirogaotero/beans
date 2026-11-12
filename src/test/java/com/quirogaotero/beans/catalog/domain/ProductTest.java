package com.quirogaotero.beans.catalog.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {

  @Test
  void a_product_must_have_a_positive_price() {
    assertThrows(IllegalArgumentException.class,
            () -> new Product("COF-COLHU-WBN-250", "Colombia Huila, 250 g", 0, "EUR"));
  }

  @Test
  void a_product_must_have_a_sku() {
    assertThrows(IllegalArgumentException.class,
            () -> new Product(" ", "Colombia Huila, 250 g", 1250, "EUR"));
  }

}
