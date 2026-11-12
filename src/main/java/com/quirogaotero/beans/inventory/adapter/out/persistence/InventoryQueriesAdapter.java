package com.quirogaotero.beans.inventory.adapter.out.persistence;

import com.quirogaotero.beans.inventory.application.port.in.LotTraceQuery;
import com.quirogaotero.beans.inventory.application.port.in.StockLevelsQuery;
import com.quirogaotero.beans.inventory.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class InventoryQueriesAdapter implements StockLevelsQuery, LotTraceQuery {

  private final JdbcTemplate jdbc;

  public InventoryQueriesAdapter(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public List<StockLevel> findBySku(SkuId sku) {
    return jdbc.query("""
            SELECT lo.id, lo.type, l.code, l.best_before, ls.on_hand, ls.reserved
            FROM lot_stocks ls
            JOIN lots l ON l.id = ls.lot_id
            JOIN locations lo ON lo.id = ls.location_id
            WHERE l.sku_id = ?
            ORDER BY lo.type DESC, lo.id, l.best_before
            """,
            (rs, i) -> new StockLevel(
                    new LocationId(rs.getString("id")),
                    LocationType.valueOf(rs.getString("type")),
                    new LotCode(rs.getString("code")),
                    rs.getObject("best_before", LocalDate.class),
                    Quantity.of(rs.getInt("on_hand")),
                    Quantity.of(rs.getInt("reserved"))),
            sku.value());
  }

  @Override
  public Optional<LotTrace> findByLotCode(LotCode lotCode) {
    List<Lot> lots = jdbc.query("SELECT id, sku_id, code, best_before FROM lots WHERE code = ?",
            (rs, i) -> new Lot(
                    new LotId(rs.getObject("id", UUID.class)),
                    new SkuId(rs.getString("sku_id")),
                    new LotCode(rs.getString("code")),
                    rs.getObject("best_before", LocalDate.class)),
            lotCode.value());
    if (lots.isEmpty())
      return Optional.empty();
    Lot lot = lots.getFirst();

    List<Shipment> shipments = jdbc.query("""
            SELECT order_id, location_id, quantity, occurred_at
            FROM stock_movements
            WHERE lot_id = ? AND type = 'PICK'
            ORDER BY occurred_at
            """,
            (rs, i) -> new Shipment(
                    rs.getObject("order_id", UUID.class),
                    new LocationId(rs.getString("location_id")),
                    Quantity.of(rs.getInt("quantity")),
                    rs.getTimestamp("occurred_at").toInstant()),
            lot.getId().value());

    return Optional.of(new LotTrace(lot.getLotCode(), lot.getSkuId(), lot.getBestBefore(), shipments));
  }

}
