package com.quirogaotero.beans.ordering.adapter.in.web;

import java.util.UUID;

public record PayResponse(boolean paid, UUID orderId, String paymentReference,
                          Long chargedInCents, String currency, String reason) {
}
