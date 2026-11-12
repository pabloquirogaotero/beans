package com.quirogaotero.beans.inventory.adapter.in.web;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record LotTraceResponse(String lotCode, String sku, LocalDate bestBefore, List<Shipment> shipments) {

  public record Shipment(UUID orderId, String shippedFrom, int quantity, Instant pickedAt) { }

}
