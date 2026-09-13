package com.quirogaotero.beans.ordering.adapter.out.persistence;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "checkouts")
public class CheckoutJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<LineJson> lines;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    protected CheckoutJpaEntity() { }

    public CheckoutJpaEntity(UUID id, String status, List<LineJson> lines, Instant startedAt) {
        this.id = id; this.status = status; this.lines = lines; this.startedAt = startedAt;
    }

    public UUID getId() { return id; }
    public String getStatus() { return status; }
    public List<LineJson> getLines() { return lines; }
    public Instant getStartedAt() { return startedAt; }
}