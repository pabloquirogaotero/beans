package com.quirogaotero.beans.inventory.application.port.out;

import com.quirogaotero.beans.inventory.domain.StockPicked;

import java.util.ArrayList;
import java.util.List;

public class InMemoryInventoryOutbox implements InventoryOutbox {

  public final List<StockPicked> events = new ArrayList<>();

  public void append(StockPicked event) { events.add(event); }

}
