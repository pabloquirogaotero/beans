package com.quirogaotero.beans.inventory.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class StockLedgerEntryTest {

  private final Instant now = Instant.parse("2026-07-01T10:00:00Z");
  private final LotStockId lotStockId = new LotStockId(new LocationId("WRH-OUR-01"), LotId.newLotId());
  private final CheckoutId checkout = CheckoutId.newCheckoutId();

  @Test
  void recordsAMovementWithTheCheckoutThatCausedIt() {
    StockMovement reserve = StockMovement.of(lotStockId, StockMovementType.RESERVE, Quantity.of(2));

    StockLedgerEntry entry = StockLedgerEntry.forCheckout(reserve, checkout, now);

    assertEquals(lotStockId, entry.lotStockId());
    assertEquals(StockMovementType.RESERVE, entry.type());
    assertEquals(Quantity.of(2), entry.quantity());
    assertEquals(checkout, entry.checkoutId());
    assertNull(entry.orderId());
    assertEquals(now, entry.occurredAt());
  }

  @Test
  void recordsAPickWithItsOrder() {
    StockMovement pick = StockMovement.of(lotStockId, StockMovementType.PICK, Quantity.of(2));
    UUID orderId = UUID.randomUUID();

    StockLedgerEntry entry = StockLedgerEntry.forOrder(pick, checkout, orderId, now);

    assertEquals(orderId, entry.orderId());
  }

  @Test
  void aPickWithoutAnOrderIsRejected() {
    StockMovement pick = StockMovement.of(lotStockId, StockMovementType.PICK, Quantity.of(2));

    assertThrows(IllegalArgumentException.class, () -> StockLedgerEntry.forCheckout(pick, checkout, now));
  }

}
