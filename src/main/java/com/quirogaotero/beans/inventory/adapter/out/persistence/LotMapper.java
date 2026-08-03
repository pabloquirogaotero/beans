package com.quirogaotero.beans.inventory.adapter.out.persistence;

import com.quirogaotero.beans.inventory.domain.*;

public final class LotMapper {

  private LotMapper() { }

  public static Lot toDomain(LotJpaEntity entity) {
    return new Lot(
            new LotId(entity.getId()),
            new SkuId(entity.getSkuId()),
            new LotCode(entity.getCode()),
            entity.getBestBefore());
  }

  public static LotJpaEntity toEntity(Lot lot) {
    return new LotJpaEntity(
            lot.getId().value(),
            lot.getSkuId().value(),
            lot.getLotCode().value(),
            lot.getBestBefore());
  }

}
