package com.quirogaotero.beans.ordering.application;


import com.quirogaotero.beans.inventory.domain.CheckoutId;

public class PaymentDeclinedException extends RuntimeException {

    public PaymentDeclinedException(CheckoutId id, String reason) {
        super("Payment declined for checkout " + id + ": " + reason);
    }

}
