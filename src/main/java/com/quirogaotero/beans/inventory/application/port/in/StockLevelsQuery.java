package com.quirogaotero.beans.inventory.application.port.in;

import com.quirogaotero.beans.inventory.domain.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Read side. A query changes nothing and protects no invariant, so it is answered straight from
 * a read model instead of loading aggregates through a service.
 */
public interface StockLevelsQuery {

  /** Every lot of the SKU at every location, including lots with nothing left available. */
  List<StockLevel> findBySku(SkuId sku);

  record StockLevel(LocationId locationId, LocationType locationType, LotCode lotCode,
                    LocalDate bestBefore, Quantity onHand, Quantity reserved) {

    public Quantity available() { return onHand.minus(reserved); }

  }

}
