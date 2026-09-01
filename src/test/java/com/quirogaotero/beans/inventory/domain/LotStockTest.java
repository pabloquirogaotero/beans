package com.quirogaotero.beans.inventory.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LotStockTest {

  @Test
  void reservingLessThanAvailableSucceedsAndDecrements() {
    LotStock stock = new LotStock(new LotStockId(new LocationId("WRH-OUR-O1"), LotId.newLotId()),
            Quantity.of(10), Quantity.of(0));
    StockMovement movement = stock.reserve(Quantity.of(4));
    assertEquals(4, stock.getReserved().value());
    assertEquals(6, stock.getAvailable().value());
    assertEquals(StockMovementType.RESERVE, movement.type());
  }

  @Test
  void reservingExactlyAvailableSucceeds() {
    LotStock stock = new LotStock(new LotStockId(new LocationId("WRH-OUR-O1"), LotId.newLotId()),
            Quantity.of(5), Quantity.of(0));
    stock.reserve(Quantity.of(5));
    assertEquals(0, stock.getAvailable().value());
  }

  @Test
  void reservingOneMoreThanAvailableThrows() {
    LotStock stock = new LotStock(new LotStockId(new LocationId("WRH-OUR-O1"), LotId.newLotId()),
            Quantity.of(3), Quantity.of(0));
    assertThrows(InsufficientStockException.class, () -> stock.reserve(Quantity.of(4)));
  }

  @Test
  void reserveIsCheckedAgainstAvailableNotOnHand() {
    LotStock stock = new LotStock(new LotStockId(new LocationId("WRH-OUR-O1"), LotId.newLotId()),
            Quantity.of(10), Quantity.of(7));
    stock.reserve(Quantity.of(3));
    assertEquals(0, stock.getAvailable().value());

    LotStock otherStock = new LotStock(new LotStockId(new LocationId("WRH-OUR-O1"), LotId.newLotId()),
            Quantity.of(10), Quantity.of(7));
    assertThrows(InsufficientStockException.class, () -> otherStock.reserve(Quantity.of(4)));
  }

  @Test
  void releasingReturnsStockToAvailable() {
    LotStock stock = new LotStock(new LotStockId(new LocationId("WRH-OUR-O1"), LotId.newLotId()),
            Quantity.of(10), Quantity.of(4));
    stock.release(Quantity.of(3));
    assertEquals(1, stock.getReserved().value());
    assertEquals(9, stock.getAvailable().value());
  }

  @Test
  void releasingMoreThanReservedThrows() {
    var stock = new LotStock(new LotStockId(new LocationId("WRH-OUR-O1"), LotId.newLotId()),
            Quantity.of(10), Quantity.of(2));
    assertThrows(IllegalArgumentException.class, () -> stock.release(Quantity.of(3)));
  }

  @Test
  void constructingWithReservedGreaterThanOnHandViolatesInvariant() {
    assertThrows(IllegalStateException.class,
            () -> new LotStock(new LotStockId(new LocationId("WRH-OUR-O1"), LotId.newLotId()),
                    Quantity.of(2), Quantity.of(5)));
  }

}
