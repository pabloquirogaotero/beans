package com.quirogaotero.beans.inventory.adapter.out.persistence;

import com.quirogaotero.beans.inventory.domain.*;

public final class LotStockMapper {

  private LotStockMapper() {}

  public static LotStock toDomain(LotStockJpaEntity entity) {
    return new LotStock(
            toDomainId(entity.getId()),
            Quantity.of(entity.getOnHand()),
            Quantity.of(entity.getReserved()),
            entity.getVersion());
  }

  public static LotStockJpaEntity toEntity(LotStock lotStock) {
    return new LotStockJpaEntity(
            toKey(lotStock.getId()),
            lotStock.getOnHand().value(),
            lotStock.getReserved().value(),
            lotStock.getVersion());
  }

  public static void applyTo(LotStock lotStock, LotStockJpaEntity entity) {
    entity.setOnHand(lotStock.getOnHand().value());
    entity.setReserved(lotStock.getReserved().value());
  }

  public static LotStockId toDomainId(LotStockKey key) {
    return new LotStockId(new LocationId(key.getLocationId()), new LotId(key.getLotId()));
  }

  public static LotStockKey toKey(LotStockId id) {
    return new LotStockKey(id.lotId().value(), id.locationId().value());
  }

}
