package com.quirogaotero.beans.inventory.adapter.in.web;

import com.quirogaotero.beans.inventory.application.port.in.LotTraceQuery;
import com.quirogaotero.beans.inventory.application.port.in.StockLevelsQuery;
import com.quirogaotero.beans.inventory.domain.LotCode;
import com.quirogaotero.beans.inventory.domain.SkuId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InventoryController {

  private final StockLevelsQuery stockLevels;
  private final LotTraceQuery lotTrace;

  public InventoryController(StockLevelsQuery stockLevels, LotTraceQuery lotTrace) {
    this.stockLevels = stockLevels;
    this.lotTrace = lotTrace;
  }

  @GetMapping("/stock/{sku}")
  public StockResponse stock(@PathVariable String sku) {
    var levels = stockLevels.findBySku(new SkuId(sku)).stream()
            .map(l -> new StockResponse.Level(l.locationId().value(), l.locationType().name(),
                    l.lotCode().value(), l.bestBefore(),
                    l.onHand().value(), l.reserved().value(), l.available().value()))
            .toList();
    return new StockResponse(sku, levels);
  }

  @GetMapping("/lots/{lotCode}/trace")
  public LotTraceResponse trace(@PathVariable String lotCode) {
    var trace = lotTrace.findByLotCode(new LotCode(lotCode)).orElseThrow(() -> new ErrorResponseException(
            HttpStatus.NOT_FOUND,
            ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Lot not found: " + lotCode), null));

    var shipments = trace.shipments().stream()
            .map(s -> new LotTraceResponse.Shipment(s.orderId(), s.shippedFrom().value(),
                    s.quantity().value(), s.pickedAt()))
            .toList();
    return new LotTraceResponse(trace.lotCode().value(), trace.skuId().value(), trace.bestBefore(), shipments);
  }

}
