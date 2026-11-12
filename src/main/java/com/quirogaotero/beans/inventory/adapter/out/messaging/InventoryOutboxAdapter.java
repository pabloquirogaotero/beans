package com.quirogaotero.beans.inventory.adapter.out.messaging;

import com.quirogaotero.beans.inventory.application.port.out.InventoryOutbox;
import com.quirogaotero.beans.inventory.domain.StockPicked;
import com.quirogaotero.beans.messaging.application.port.out.Outbox;
import com.quirogaotero.beans.messaging.application.port.out.OutboxMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class InventoryOutboxAdapter implements InventoryOutbox {

  public static final String STOCK_PICKED = "StockPicked";

  private final Outbox outbox;
  private final JsonMapper json;
  private final String topic;

  public InventoryOutboxAdapter(Outbox outbox, JsonMapper json,
                                @Value("${beans.messaging.topics.stock-events}") String topic) {
    this.outbox = outbox;
    this.json = json;
    this.topic = topic;
  }

  // Keyed by order id, so all stock events of one order stay in order on the topic.
  @Override
  public void append(StockPicked event) {
    outbox.append(new OutboxMessage(event.eventId(), topic, STOCK_PICKED,
            event.orderId().toString(),
            json.writeValueAsString(StockPickedJson.from(event)), event.occurredAt()));
  }

}
