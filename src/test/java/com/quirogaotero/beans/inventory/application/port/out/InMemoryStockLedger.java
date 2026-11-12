package com.quirogaotero.beans.inventory.application.port.out;

import com.quirogaotero.beans.inventory.domain.StockLedgerEntry;

import java.util.ArrayList;
import java.util.List;

public class InMemoryStockLedger implements StockLedger {

  public final List<StockLedgerEntry> entries = new ArrayList<>();

  public void record(StockLedgerEntry entry) { entries.add(entry); }

}
