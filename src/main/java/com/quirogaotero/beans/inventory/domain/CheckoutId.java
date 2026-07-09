package com.quirogaotero.beans.inventory.domain;

import java.util.Objects;
import java.util.UUID;

public record CheckoutId(UUID value) {

  public CheckoutId {
    Objects.requireNonNull(value, "CheckoutId must not be null.");
  }

  public static CheckoutId newCheckoutId() {
    return new CheckoutId(UUID.randomUUID());
  }

}
