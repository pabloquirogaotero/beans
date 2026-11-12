package com.quirogaotero.beans.ordering.domain;

import com.quirogaotero.beans.inventory.domain.CheckoutId;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Checkout {

    private final CheckoutId id;
    private final List<OrderLine> lines;
    private final Money total;
    private CheckoutStatus status;
    private final Instant startedAt;

    public Checkout(CheckoutId id, List<OrderLine> lines, Money total, CheckoutStatus status, Instant startedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.total = Objects.requireNonNull(total, "total");
        this.status = Objects.requireNonNull(status, "status");
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt");
        Objects.requireNonNull(lines, "lines");
        if (lines.isEmpty())
            throw new IllegalArgumentException("A checkout must have at least one line.");
        this.lines = List.copyOf(lines);
    }

    /**
     * The total is fixed here, at the prices of the moment the customer starts paying: a price
     * change during the hold must not change what they are charged.
     */
    public static Checkout start(List<OrderLine> lines, Money total, Instant startedAt) {
        return new Checkout(CheckoutId.newCheckoutId(), lines, total, CheckoutStatus.STARTED, startedAt);
    }

    public void markPaid() {
        if (status != CheckoutStatus.STARTED)
            throw new IllegalStateException("Only a STARTED checkout can be paid (was " + status + ").");
        status = CheckoutStatus.PAID;
    }

    public void markAbandoned() {
        if (status != CheckoutStatus.STARTED)
            throw new IllegalStateException("Only a STARTED checkout can be abandoned (was " + status + ").");
        status = CheckoutStatus.ABANDONED;
    }

    public CheckoutId getId()          { return id; }
    public List<OrderLine> getLines()  { return lines; }
    public Money getTotal()            { return total; }
    public CheckoutStatus getStatus()  { return status; }
    public Instant getStartedAt()      { return startedAt; }

    @Override public boolean equals(Object o) { return o instanceof Checkout other && id.equals(other.id); }
    @Override public int hashCode() { return id.hashCode(); }

}
