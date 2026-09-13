package com.quirogaotero.beans.ordering.adapter.out.persistence;

import com.quirogaotero.beans.inventory.domain.CheckoutId;
import com.quirogaotero.beans.ordering.domain.*;
import java.util.List;

public final class OrderMapper {

    private OrderMapper() { }

    public static Order toDomain(OrderJpaEntity e) {
        List<OrderLine> lines = e.getLines().stream()
                .map(l -> new OrderLine(l.sku(), l.quantity())).toList();
        return new Order(new OrderId(e.getId()), new CheckoutId(e.getCheckoutId()),
                lines, OrderStatus.valueOf(e.getStatus()), e.getPlacedAt());
    }

    public static OrderJpaEntity toEntity(Order o) {
        List<LineJson> lines = o.getLines().stream()
                .map(l -> new LineJson(l.sku(), l.quantity())).toList();
        return new OrderJpaEntity(o.getId().value(), o.getCheckoutId().value(),
                o.getStatus().name(), lines, o.getPlacedAt());
    }

}