package com.quirogaotero.beans.ordering.adapter.in.web;

import java.time.Instant;
import java.util.List;

public record CheckoutResponse(String checkoutId, List<String> reservationIds,
                               Instant expiresAt, String reason) {
}
