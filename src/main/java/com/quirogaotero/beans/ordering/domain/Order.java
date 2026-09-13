package com.quirogaotero.beans.ordering.domain;

import com.quirogaotero.beans.inventory.domain.CheckoutId;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Order {

    private final OrderId id;
    private final CheckoutId checkoutId;
    private final List<OrderLine> lines;
    private OrderStatus status;
    private final Instant placedAt;

    public Order(OrderId id, CheckoutId checkoutId, List<OrderLine> lines,
                 OrderStatus status, Instant placedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.checkoutId = Objects.requireNonNull(checkoutId, "checkoutId");
        this.status = Objects.requireNonNull(status, "status");
        this.placedAt = Objects.requireNonNull(placedAt, "placedAt");
        Objects.requireNonNull(lines, "lines");
        if (lines.isEmpty())
            throw new IllegalArgumentException("An order must have at least one line.");
        this.lines = List.copyOf(lines);
    }

    public static Order place(CheckoutId checkoutId, List<OrderLine> lines, Instant placedAt) {
        return new Order(OrderId.newOrderId(), checkoutId, lines, OrderStatus.CONFIRMED, placedAt);
    }

    public void markFulfilled() {
        if (status != OrderStatus.CONFIRMED)
            throw new IllegalStateException("Only a CONFIRMED order can be fulfilled (was " + status + ").");
        status = OrderStatus.FULFILLED;
    }

    public OrderId getId()          { return id; }
    public CheckoutId getCheckoutId() { return checkoutId; }
    public List<OrderLine> getLines() { return lines; }
    public OrderStatus getStatus()  { return status; }
    public Instant getPlacedAt()    { return placedAt; }

    @Override public boolean equals(Object o) { return o instanceof Order other && id.equals(other.id); }
    @Override public int hashCode() { return id.hashCode(); }

}