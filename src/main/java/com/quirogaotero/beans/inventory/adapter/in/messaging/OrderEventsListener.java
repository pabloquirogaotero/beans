package com.quirogaotero.beans.inventory.adapter.in.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.quirogaotero.beans.inventory.application.port.in.PickForOrderCommand;
import com.quirogaotero.beans.inventory.application.port.in.PickForOrderUseCase;
import com.quirogaotero.beans.inventory.domain.CheckoutId;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class OrderEventsListener {

  private final PickForOrderUseCase pickForOrder;
  private final JsonMapper json;

  public OrderEventsListener(PickForOrderUseCase pickForOrder, JsonMapper json) {
    this.pickForOrder = pickForOrder;
    this.json = json;
  }

  @KafkaListener(topics = "${beans.messaging.topics.order-events}", groupId = "inventory")
  public void onOrderEvent(ConsumerRecord<String, String> record) {
    if (!"OrderPlaced".equals(eventType(record)))
      return;

    OrderPlacedMessage message = json.readValue(record.value(), OrderPlacedMessage.class);
    pickForOrder.pickForOrder(new PickForOrderCommand(
            message.eventId(), message.orderId(), new CheckoutId(message.checkoutId())));
  }

  /** Inventory's own view of Ordering's event: only the fields it needs, the rest is ignored. */
  @JsonIgnoreProperties(ignoreUnknown = true)
  record OrderPlacedMessage(UUID eventId, UUID orderId, UUID checkoutId) { }

  private static String eventType(ConsumerRecord<String, String> record) {
    Header header = record.headers().lastHeader("event-type");
    return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
  }

}
