package com.quirogaotero.beans.ordering.adapter.in.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.quirogaotero.beans.ordering.application.FulfilOrderService;
import com.quirogaotero.beans.ordering.domain.OrderId;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class StockEventsListener {

    private final FulfilOrderService fulfilOrder;
    private final JsonMapper json;

    public StockEventsListener(FulfilOrderService fulfilOrder, JsonMapper json) {
        this.fulfilOrder = fulfilOrder;
        this.json = json;
    }

    @KafkaListener(topics = "${beans.messaging.topics.stock-events}", groupId = "ordering")
    public void onStockEvent(ConsumerRecord<String, String> record) {
        if (!"StockPicked".equals(eventType(record)))
            return;

        StockPickedMessage message = json.readValue(record.value(), StockPickedMessage.class);
        fulfilOrder.fulfil(message.eventId(), new OrderId(message.orderId()));
    }

    /** Ordering's own view of Inventory's event: only the fields it needs, the rest is ignored. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record StockPickedMessage(UUID eventId, UUID orderId) { }

    private static String eventType(ConsumerRecord<String, String> record) {
        Header header = record.headers().lastHeader("event-type");
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }

}
