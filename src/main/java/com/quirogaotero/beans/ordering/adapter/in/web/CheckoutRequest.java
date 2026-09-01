package com.quirogaotero.beans.ordering.adapter.in.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record CheckoutRequest(
        @NotNull Double latitude,
        @NotNull Double longitude,
        @NotEmpty @Valid List<Line> lines) {

  public record Line(@NotBlank String sku, @Min(1) int quantity) {}

}
