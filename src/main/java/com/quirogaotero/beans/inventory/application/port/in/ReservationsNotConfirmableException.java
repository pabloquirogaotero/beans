package com.quirogaotero.beans.inventory.application.port.in;

import com.quirogaotero.beans.inventory.domain.CheckoutId;

public class ReservationsNotConfirmableException extends RuntimeException {

    public ReservationsNotConfirmableException(CheckoutId checkoutId) {
        super("Reservations for checkout " + checkoutId + " cannot be confirmed (expired or not active).");
    }

}
