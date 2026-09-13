package com.quirogaotero.beans.ordering.adapter.in.web;

import com.quirogaotero.beans.inventory.domain.CheckoutId;
import com.quirogaotero.beans.ordering.application.PayCheckoutResult;
import com.quirogaotero.beans.ordering.application.PayCheckoutService;
import com.quirogaotero.beans.ordering.domain.Money;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/checkout")
public class PayCheckoutController {

    private final PayCheckoutService payCheckout;

    public PayCheckoutController(PayCheckoutService payCheckout) {
        this.payCheckout = payCheckout;
    }

    @PostMapping("/{checkoutId}/pay")
    public ResponseEntity<PayResponse> pay(@PathVariable UUID checkoutId,
                                           @Valid @RequestBody PayRequest request) {
        PayCheckoutResult result = payCheckout.pay(
                new CheckoutId(checkoutId),
                new Money(request.amountInCents(), request.currency()));

        if (!result.paid()) {
            // Reservation expired -> 409 Conflict (the hold is gone, retry the checkout).
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new PayResponse(false, null, null, result.reason()));
        }

        return ResponseEntity.ok(new PayResponse(
                true, result.orderId().value(), result.paymentReference(), null));
    }

}
