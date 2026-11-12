package com.quirogaotero.beans.inventory.application.port.out;

import com.quirogaotero.beans.inventory.domain.StockPicked;

/**
 * Where Inventory announces what happened. An event appended here is stored in the same
 * database transaction as the stock change it describes, and published to the broker afterwards.
 */
public interface InventoryOutbox {

  void append(StockPicked event);

}
