package com.quirogaotero.beans.inventory.adapter.out.persistence;

import com.quirogaotero.beans.inventory.domain.*;

public final class ReservationMapper {

  private ReservationMapper() { }

  public static Reservation toDomain(ReservationJpaEntity entity) {
    return new Reservation(
            new ReservationId(entity.getId()),
            new CheckoutId(entity.getCheckoutId()),
            new LotStockId(new LocationId(entity.getLocationId()),
                    new LotId(entity.getLotId())),
            Quantity.of(entity.getQuantity()),
            entity.getExpiresAt(),
            entity.getStatus());
  }

  public static ReservationJpaEntity toEntity(Reservation reservation) {
    LotStockId lotStockId = reservation.getLotStockId();
    return new ReservationJpaEntity(
            reservation.getId().value(),
            reservation.getCheckoutId().value(),
            lotStockId.locationId().value(),
            lotStockId.lotId().value(),
            reservation.getQuantity().value(),
            reservation.getExpiresAt(),
            reservation.getStatus());
  }

  public static void applyTo(Reservation reservation, ReservationJpaEntity entity) {
    entity.setStatus(reservation.getStatus());
  }

}
