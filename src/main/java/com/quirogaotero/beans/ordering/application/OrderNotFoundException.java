package com.quirogaotero.beans.ordering.application;

import com.quirogaotero.beans.ordering.domain.OrderId;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(OrderId id) { super("Order not found: " + id.value()); }

}
