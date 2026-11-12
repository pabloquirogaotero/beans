package com.quirogaotero.beans.ordering.application;

import com.quirogaotero.beans.inventory.domain.CheckoutId;
import com.quirogaotero.beans.messaging.application.port.out.InMemoryProcessedEvents;
import com.quirogaotero.beans.ordering.domain.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FulfilOrderServiceTest {

    private final InMemoryOrderRepository orders = new InMemoryOrderRepository();
    private final FulfilOrderService service = new FulfilOrderService(orders, new InMemoryProcessedEvents());

    private Order placedOrder() {
        Order order = Order.place(CheckoutId.newCheckoutId(),
                List.of(new OrderLine("COF-COLHU-WBN-250", 2)), Money.euros(2500), Instant.parse("2026-07-01T10:00:00Z"));
        orders.save(order);
        return order;
    }

    @Test
    void marks_the_order_fulfilled() {
        Order order = placedOrder();

        service.fulfil(UUID.randomUUID(), order.getId());

        assertEquals(OrderStatus.FULFILLED, orders.findById(order.getId()).orElseThrow().getStatus());
    }

    @Test
    void ignores_an_event_it_has_already_handled() {
        Order order = placedOrder();
        UUID eventId = UUID.randomUUID();

        service.fulfil(eventId, order.getId());

        assertDoesNotThrow(() -> service.fulfil(eventId, order.getId()));
        assertEquals(OrderStatus.FULFILLED, orders.findById(order.getId()).orElseThrow().getStatus());
    }

    @Test
    void fails_when_the_order_does_not_exist() {
        assertThrows(IllegalStateException.class,
                () -> service.fulfil(UUID.randomUUID(), OrderId.newOrderId()));
    }

}
