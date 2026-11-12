package com.quirogaotero.beans.ordering.adapter.in.web;

import com.quirogaotero.beans.inventory.domain.CheckoutId;
import com.quirogaotero.beans.ordering.application.PayCheckoutResult;
import com.quirogaotero.beans.ordering.application.PayCheckoutService;
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

    // No amount in the request: the server charges the total the checkout was priced at.
    @PostMapping("/{checkoutId}/pay")
    public ResponseEntity<PayResponse> pay(@PathVariable UUID checkoutId) {
        PayCheckoutResult result = payCheckout.pay(new CheckoutId(checkoutId));

        if (!result.paid()) {
            // Reservation expired -> 409 Conflict (the hold is gone, retry the checkout).
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new PayResponse(false, null, null, null, null, result.reason()));
        }

        return ResponseEntity.ok(new PayResponse(true, result.orderId().value(), result.paymentReference(),
                result.charged().amountInCents(), result.charged().currency(), null));
    }

}
