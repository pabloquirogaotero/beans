package com.quirogaotero.beans.inventory;

import com.quirogaotero.beans.inventory.domain.*;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Creates inventory rows that belong to a single test. Every id is random, so integration
 * tests sharing one database never see each other's stock, and nothing depends on the demo
 * seed or on today's date.
 */
@TestComponent
public class InventoryFixture {

  private final JdbcTemplate jdbc;

  public InventoryFixture(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public SkuId newSku() {
    return new SkuId("TST-" + random(12));
  }

  public LocationId newWarehouse(double latitude, double longitude) {
    LocationId id = new LocationId("WRH-" + random(6));
    jdbc.update("INSERT INTO locations (id, type, latitude, longitude) VALUES (?, 'WAREHOUSE', ?, ?)",
            id.value(), latitude, longitude);
    return id;
  }

  /** A lot of the given SKU, comfortably inside its shelf life, with stock at the location. */
  public LotStockId stockLot(SkuId sku, LocationId location, int onHand) {
    return stockLot(sku, location, onHand, LocalDate.now().plusYears(1));
  }

  public LotStockId stockLot(SkuId sku, LocationId location, int onHand, LocalDate bestBefore) {
    LotId lotId = LotId.newLotId();
    jdbc.update("INSERT INTO lots (id, sku_id, code, best_before) VALUES (?, ?, ?, ?)",
            lotId.value(), sku.value(), "L" + random(8), bestBefore);
    jdbc.update("INSERT INTO lot_stocks (lot_id, location_id, on_hand) VALUES (?, ?, ?)",
            lotId.value(), location.value(), onHand);
    return new LotStockId(location, lotId);
  }

  private static String random(int length) {
    return UUID.randomUUID().toString().replace("-", "").substring(0, length).toUpperCase();
  }

}
