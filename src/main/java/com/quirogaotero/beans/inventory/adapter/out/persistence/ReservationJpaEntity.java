package com.quirogaotero.beans.inventory.adapter.out.persistence;

import com.quirogaotero.beans.inventory.domain.ReservationStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class ReservationJpaEntity {

  @Id
  private UUID id;

  @Column(name = "checkout_id", nullable = false)
  private UUID checkoutId;

  @Column(name = "lot_id", nullable = false)
  private UUID lotId;

  @Column(name = "location_id", nullable = false)
  private String locationId;

  @Column(nullable = false)
  private int quantity;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReservationStatus status;

  @Version
  @Column(nullable = false)
  private long version;

  protected ReservationJpaEntity() { }

  public ReservationJpaEntity(UUID id, UUID checkoutId, String locationId, UUID lotId,
                              int quantity, Instant expiresAt, ReservationStatus status) {
    this.id = id;
    this.checkoutId = checkoutId;
    this.locationId = locationId;
    this.lotId = lotId;
    this.quantity = quantity;
    this.expiresAt = expiresAt;
    this.status = status;
  }

  public UUID getId() { return id; }
  public UUID getCheckoutId() { return checkoutId; }
  public String getLocationId() { return locationId; }
  public UUID getLotId() { return lotId; }
  public int getQuantity() { return quantity; }
  public Instant getExpiresAt() { return expiresAt; }
  public ReservationStatus getStatus() { return status; }
  public long getVersion() { return version; }

  public void setStatus(ReservationStatus status) { this.status = status; }

}