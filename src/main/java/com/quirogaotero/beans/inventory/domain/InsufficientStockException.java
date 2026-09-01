package com.quirogaotero.beans.inventory.domain;

public class InsufficientStockException extends RuntimeException {

  public InsufficientStockException(LotStockId id, Quantity requested, Quantity available) {
    super("Insufficient stock for %s. Requested: %d. Available: %d."
            .formatted(id, requested.value(), available.value()));
  }

}
