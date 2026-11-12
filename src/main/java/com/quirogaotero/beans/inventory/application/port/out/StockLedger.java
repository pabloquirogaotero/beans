package com.quirogaotero.beans.inventory.application.port.out;

import com.quirogaotero.beans.inventory.domain.StockLedgerEntry;

/** Append-only record of every stock movement. Entries join the caller's transaction. */
public interface StockLedger {

  void record(StockLedgerEntry entry);

}
