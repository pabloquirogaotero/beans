package com.quirogaotero.beans.inventory.adapter.in.web;

import java.time.LocalDate;
import java.util.List;

public record StockResponse(String sku, List<Level> levels) {

  public record Level(String location, String locationType, String lotCode, LocalDate bestBefore,
                      int onHand, int reserved, int available) { }

}
