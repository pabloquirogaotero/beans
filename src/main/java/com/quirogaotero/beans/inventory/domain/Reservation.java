package com.quirogaotero.beans.inventory.domain;

import java.time.Instant;
import java.util.Objects;

public class Reservation {

  private final ReservationId id;
  private final CheckoutId checkoutId;
  private final LotStockId lotStockId;
  private final Quantity quantity;
  private final Instant expiresAt;
  private ReservationStatus status;

  public Reservation(ReservationId id, CheckoutId checkoutId, LotStockId lotStockId, Quantity quantity,
                     Instant expiresAt, ReservationStatus status) {
    this.id = Objects.requireNonNull(id, "Id must not be null.");
    this.checkoutId = Objects.requireNonNull(checkoutId, "CheckoutId must not be null.");
    this.lotStockId = Objects.requireNonNull(lotStockId, "LotStockId must not be null.");
    this.quantity = Objects.requireNonNull(quantity, "Quantity must not be null.");
    this.expiresAt = Objects.requireNonNull(expiresAt, "ExpiresAt must not be null.");
    this.status = Objects.requireNonNull(status, "Status must not be null.");
  }

  public static Reservation newActiveReservation(CheckoutId checkoutId, LotStockId lotStockId, Quantity quantity,
                                                 Instant expiresAt) {
    return new Reservation(ReservationId.newReservationId(), checkoutId, lotStockId, quantity, expiresAt,
            ReservationStatus.ACTIVE);
  }

  public void confirm(Instant now) {
    if (this.status != ReservationStatus.ACTIVE)
      throw new IllegalStateException("Reservation " + this.id + " is not active.");
    if (now.isAfter(this.expiresAt))
      throw new IllegalStateException("Reservation " + this.id + " has expired.");

    this.status = ReservationStatus.CONFIRMED;
  }

  public void release() {
    if (this.status != ReservationStatus.ACTIVE)
      throw new IllegalStateException("Reservation " + this.id + " is not active.");

    this.status = ReservationStatus.RELEASED;
  }

  public boolean isExpired(Instant now) { return now.isAfter(this.expiresAt); }

  public ReservationId getId() { return this.id; }
  public CheckoutId getCheckoutId() { return this.checkoutId; }
  public LotStockId getLotStockId() { return this.lotStockId; }
  public Quantity getQuantity() { return this.quantity; }
  public Instant getExpiresAt() { return this.expiresAt; }
  public ReservationStatus getStatus() { return this.status; }

}
