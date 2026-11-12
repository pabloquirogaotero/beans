package com.quirogaotero.beans.messaging.adapter.out.persistence;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEventJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private String type;

    @Column(name = "message_key", nullable = false)
    private String messageKey;

    // Already-serialised JSON: the relay forwards it to the broker without parsing it.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    protected OutboxEventJpaEntity() { }

    public OutboxEventJpaEntity(UUID id, String topic, String type, String messageKey,
                                String payload, Instant occurredAt) {
        this.id = id; this.topic = topic; this.type = type; this.messageKey = messageKey;
        this.payload = payload; this.occurredAt = occurredAt;
    }

    public UUID getId() { return id; }
    public String getTopic() { return topic; }
    public String getType() { return type; }
    public String getMessageKey() { return messageKey; }
    public String getPayload() { return payload; }
    public Instant getOccurredAt() { return occurredAt; }
    public Instant getPublishedAt() { return publishedAt; }

    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }

}
