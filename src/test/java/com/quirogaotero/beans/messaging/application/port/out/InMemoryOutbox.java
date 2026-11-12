package com.quirogaotero.beans.messaging.application.port.out;

import java.time.Instant;
import java.util.*;

public class InMemoryOutbox implements Outbox {

    public final List<OutboxMessage> messages = new ArrayList<>();
    public final Map<UUID, Instant> publishedAt = new HashMap<>();

    public void append(OutboxMessage message) { messages.add(message); }

    public List<OutboxMessage> findPending(int limit) {
        return messages.stream()
                .filter(m -> !publishedAt.containsKey(m.id()))
                .sorted(Comparator.comparing(OutboxMessage::occurredAt))
                .limit(limit)
                .toList();
    }

    public void markPublished(UUID messageId, Instant at) { publishedAt.put(messageId, at); }

}
