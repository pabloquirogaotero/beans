package com.quirogaotero.beans.ordering.domain;

public record OrderLine(String sku, int quantity) {

    public OrderLine {
        if (sku == null || sku.isBlank())
            throw new IllegalArgumentException("OrderLine sku must not be blank.");
        if (quantity <= 0)
            throw new IllegalArgumentException("OrderLine quantity must be positive.");
    }

}
