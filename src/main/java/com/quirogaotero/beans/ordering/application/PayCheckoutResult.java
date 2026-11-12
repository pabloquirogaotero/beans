package com.quirogaotero.beans.ordering.application;

import com.quirogaotero.beans.ordering.domain.Money;
import com.quirogaotero.beans.ordering.domain.OrderId;

public record PayCheckoutResult(boolean paid, OrderId orderId, String paymentReference, Money charged, String reason) {

    public static PayCheckoutResult paid(OrderId orderId, String reference, Money charged) {
        return new PayCheckoutResult(true, orderId, reference, charged, null);
    }

    public static PayCheckoutResult reservationExpired() {
        return new PayCheckoutResult(false, null, null, null, "Reservation expired");
    }

}
