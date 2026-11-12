package com.quirogaotero.beans.inventory.application.port.in;

import com.quirogaotero.beans.inventory.domain.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The recall question: a lot turned out bad, which orders received units of it? Answered
 * from the stock ledger's PICK entries. Read side, like {@link StockLevelsQuery}.
 */
public interface LotTraceQuery {

  Optional<LotTrace> findByLotCode(LotCode lotCode);

  record LotTrace(LotCode lotCode, SkuId skuId, LocalDate bestBefore, List<Shipment> shipments) {

    public LotTrace { shipments = List.copyOf(shipments); }

  }

  /** Units of the lot that left a location in an order. */
  record Shipment(UUID orderId, LocationId shippedFrom, Quantity quantity, Instant pickedAt) { }

}
