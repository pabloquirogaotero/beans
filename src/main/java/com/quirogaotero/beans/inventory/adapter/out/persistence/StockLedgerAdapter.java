package com.quirogaotero.beans.inventory.adapter.out.persistence;

import com.quirogaotero.beans.inventory.application.port.out.StockLedger;
import com.quirogaotero.beans.inventory.domain.StockLedgerEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

// Plain JDBC: ledger rows are only ever inserted, so there is nothing for an entity to track.
@Repository
public class StockLedgerAdapter implements StockLedger {

  private final JdbcTemplate jdbc;

  public StockLedgerAdapter(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public void record(StockLedgerEntry entry) {
    jdbc.update("""
            INSERT INTO stock_movements (id, lot_id, location_id, type, quantity, checkout_id, order_id, occurred_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """,
            entry.id(),
            entry.lotStockId().lotId().value(),
            entry.lotStockId().locationId().value(),
            entry.type().name(),
            entry.quantity().value(),
            entry.checkoutId().value(),
            entry.orderId(),
            Timestamp.from(entry.occurredAt()));
  }

}
