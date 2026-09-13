package com.quirogaotero.beans.ordering.domain;

import java.util.Objects;
import java.util.UUID;

public record OrderId(UUID value) {

    public OrderId { Objects.requireNonNull(value, "OrderId value must not be null."); }

    public static OrderId newOrderId() { return new OrderId(UUID.randomUUID()); }

}
