package com.quirogaotero.beans.ordering.adapter.in.web;

import com.quirogaotero.beans.ordering.application.CheckoutOutcome;
import com.quirogaotero.beans.ordering.application.StartCheckoutCommand;
import com.quirogaotero.beans.ordering.application.StartCheckoutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

  private final StartCheckoutService startCheckout;

  public CheckoutController(StartCheckoutService startCheckout) {
    this.startCheckout = startCheckout;
  }

  @PostMapping
  public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
    var command = new StartCheckoutCommand(
            request.latitude(), request.longitude(),
            request.lines().stream()
                    .map(l -> new StartCheckoutCommand.Line(l.sku(), l.quantity()))
                    .toList());

    CheckoutOutcome outcome = startCheckout.startCheckout(command);

    if (!outcome.result().reserved()) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
              .body(new CheckoutResponse(null, List.of(), null, outcome.result().reason()));
    }

    var response = new CheckoutResponse(
            outcome.checkoutId().value().toString(),
            outcome.result().reservationIds().stream().map(id -> id.value().toString()).toList(),
            outcome.result().expiresAt(),
            null);
    return ResponseEntity.ok(response);
  }

}
