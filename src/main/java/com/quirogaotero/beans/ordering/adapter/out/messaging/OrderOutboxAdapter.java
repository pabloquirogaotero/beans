package com.quirogaotero.beans.ordering.adapter.out.messaging;

import com.quirogaotero.beans.messaging.application.port.out.Outbox;
import com.quirogaotero.beans.messaging.application.port.out.OutboxMessage;
import com.quirogaotero.beans.ordering.application.port.out.OrderOutbox;
import com.quirogaotero.beans.ordering.domain.OrderPlaced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class OrderOutboxAdapter implements OrderOutbox {

    public static final String ORDER_PLACED = "OrderPlaced";

    private final Outbox outbox;
    private final JsonMapper json;
    private final String topic;

    public OrderOutboxAdapter(Outbox outbox, JsonMapper json,
                              @Value("${beans.messaging.topics.order-events}") String topic) {
        this.outbox = outbox;
        this.json = json;
        this.topic = topic;
    }

    // Keyed by order id, so all events of one order stay in order on the topic.
    @Override public void append(OrderPlaced event) {
        outbox.append(new OutboxMessage(event.eventId(), topic, ORDER_PLACED,
                event.orderId().value().toString(),
                json.writeValueAsString(OrderPlacedJson.from(event)), event.occurredAt()));
    }

}
