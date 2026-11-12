package com.quirogaotero.beans.ordering.adapter.in.web;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(UUID orderId, UUID checkoutId, String status, List<Line> lines, Instant placedAt) {

    public record Line(String sku, int quantity) { }

}
