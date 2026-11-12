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

    @Column(name = "total_in_cents", nullable = false)
    private long totalInCents;

    @Column(nullable = false)
    private String currency;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    protected CheckoutJpaEntity() { }

    public CheckoutJpaEntity(UUID id, String status, List<LineJson> lines,
                             long totalInCents, String currency, Instant startedAt) {
        this.id = id; this.status = status; this.lines = lines;
        this.totalInCents = totalInCents; this.currency = currency; this.startedAt = startedAt;
    }

    public UUID getId() { return id; }
    public String getStatus() { return status; }
    public List<LineJson> getLines() { return lines; }
    public long getTotalInCents() { return totalInCents; }
    public String getCurrency() { return currency; }
    public Instant getStartedAt() { return startedAt; }
}