package com.quirogaotero.beans.ordering.adapter.out.persistence;

import com.quirogaotero.beans.inventory.domain.CheckoutId;
import com.quirogaotero.beans.ordering.domain.*;

import java.util.List;

public final class CheckoutMapper {

    private CheckoutMapper() { }

    public static Checkout toDomain(CheckoutJpaEntity e) {
        List<OrderLine> lines = e.getLines().stream()
                .map(l -> new OrderLine(l.sku(), l.quantity())).toList();
        return new Checkout(new CheckoutId(e.getId()), lines, new Money(e.getTotalInCents(), e.getCurrency()),
                CheckoutStatus.valueOf(e.getStatus()), e.getStartedAt());
    }

    public static CheckoutJpaEntity toEntity(Checkout c) {
        List<LineJson> lines = c.getLines().stream()
                .map(l -> new LineJson(l.sku(), l.quantity())).toList();
        return new CheckoutJpaEntity(c.getId().value(), c.getStatus().name(), lines,
                c.getTotal().amountInCents(), c.getTotal().currency(), c.getStartedAt());
    }

}
