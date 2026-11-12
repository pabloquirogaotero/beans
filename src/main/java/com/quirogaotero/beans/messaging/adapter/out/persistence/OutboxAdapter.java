package com.quirogaotero.beans.messaging.adapter.out.persistence;

import com.quirogaotero.beans.messaging.application.port.out.Outbox;
import com.quirogaotero.beans.messaging.application.port.out.OutboxMessage;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class OutboxAdapter implements Outbox {

    private final OutboxEventJpaRepository jpa;

    public OutboxAdapter(OutboxEventJpaRepository jpa) { this.jpa = jpa; }

    @Override public void append(OutboxMessage m) {
        jpa.save(new OutboxEventJpaEntity(m.id(), m.topic(), m.type(), m.key(), m.payload(), m.occurredAt()));
    }

    @Override public List<OutboxMessage> findPending(int limit) {
        return jpa.findPendingForUpdate(limit).stream()
                .map(e -> new OutboxMessage(e.getId(), e.getTopic(), e.getType(), e.getMessageKey(),
                        e.getPayload(), e.getOccurredAt()))
                .toList();
    }

    @Override public void markPublished(UUID messageId, Instant publishedAt) {
        OutboxEventJpaEntity entity = jpa.findById(messageId).orElseThrow(
                () -> new IllegalStateException("Outbox message not found: " + messageId));
        entity.setPublishedAt(publishedAt);
        jpa.save(entity);
    }

}
