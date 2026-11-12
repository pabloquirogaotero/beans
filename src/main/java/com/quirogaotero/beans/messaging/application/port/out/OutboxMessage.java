package com.quirogaotero.beans.messaging.application.port.out;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A message waiting in the outbox for the broker. The payload is opaque here: only the context
 * that wrote it knows its format. Messages with the same key keep their relative order.
 */
public record OutboxMessage(UUID id, String topic, String type, String key, String payload, Instant occurredAt) {

    public OutboxMessage {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(topic, "topic");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(payload, "payload");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }

}
