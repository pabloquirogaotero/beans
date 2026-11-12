package com.quirogaotero.beans.inventory.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StockPickedTest {

  private final Instant now = Instant.parse("2026-07-01T10:00:00Z");
  private final StockPicked.Line line = new StockPicked.Line(
          new LotStockId(new LocationId("WRH-OUR-01"), LotId.newLotId()), Quantity.of(2));

  @Test
  void mustHaveAtLeastOneLine() {
    assertThrows(IllegalArgumentException.class,
            () -> new StockPicked(UUID.randomUUID(), UUID.randomUUID(), CheckoutId.newCheckoutId(), List.of(), now));
  }

  @Test
  void linesAreCopiedAndImmutable() {
    List<StockPicked.Line> lines = new ArrayList<>(List.of(line));
    StockPicked event = new StockPicked(UUID.randomUUID(), UUID.randomUUID(), CheckoutId.newCheckoutId(), lines, now);
    lines.clear();
    assertEquals(1, event.lines().size());
    assertThrows(UnsupportedOperationException.class, () -> event.lines().add(line));
  }

}
