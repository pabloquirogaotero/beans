package com.quirogaotero.beans.inventory.application.port.in;

import com.quirogaotero.beans.inventory.domain.CheckoutId;

import java.util.Objects;
import java.util.UUID;

/** {@code eventId} identifies the event that asked for the pick, so a repeat can be recognised. */
public record PickForOrderCommand(UUID eventId, UUID orderId, CheckoutId checkoutId) {

  public PickForOrderCommand {
    Objects.requireNonNull(eventId, "EventId must not be null.");
    Objects.requireNonNull(orderId, "OrderId must not be null.");
    Objects.requireNonNull(checkoutId, "CheckoutId must not be null.");
  }

}
