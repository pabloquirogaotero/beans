package com.quirogaotero.beans.ordering.application;

import com.quirogaotero.beans.messaging.application.port.out.ProcessedEvents;
import com.quirogaotero.beans.ordering.application.port.out.OrderRepository;
import com.quirogaotero.beans.ordering.domain.Order;
import com.quirogaotero.beans.ordering.domain.OrderId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FulfilOrderService {

    static final String CONSUMER = "ordering.fulfil-order";

    private final OrderRepository orders;
    private final ProcessedEvents processedEvents;

    public FulfilOrderService(OrderRepository orders, ProcessedEvents processedEvents) {
        this.orders = orders;
        this.processedEvents = processedEvents;
    }

    /** Marks the order fulfilled in response to the event that reported its stock picked. */
    @Transactional
    public void fulfil(UUID eventId, OrderId orderId) {
        // A redelivered event is skipped rather than failing on an already-fulfilled order.
        if (!processedEvents.markProcessed(CONSUMER, eventId))
            return;

        Order order = orders.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));
        order.markFulfilled();
        orders.save(order);
    }

}
