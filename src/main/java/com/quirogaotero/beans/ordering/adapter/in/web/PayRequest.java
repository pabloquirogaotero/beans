package com.quirogaotero.beans.ordering.adapter.in.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PayRequest(
        @Min(0) long amountInCents,
        @NotBlank String currency) {
}
