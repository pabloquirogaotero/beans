package com.quirogaotero.beans.ordering.application;

import com.quirogaotero.beans.ordering.domain.OrderId;

public record PayCheckoutResult(boolean paid, OrderId orderId, String paymentReference, String reason) {

    public static PayCheckoutResult paid(OrderId orderId, String reference) {
        return new PayCheckoutResult(true, orderId, reference, null);
    }

    public static PayCheckoutResult reservationExpired() {
        return new PayCheckoutResult(false, null, null, "Reservation expired");
    }

}
