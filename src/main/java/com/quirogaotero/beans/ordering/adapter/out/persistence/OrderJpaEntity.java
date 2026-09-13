package com.quirogaotero.beans.ordering.adapter.out.persistence;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    private UUID id;

    @Column(name = "checkout_id", nullable = false)
    private UUID checkoutId;

    @Column(nullable = false)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<LineJson> lines;

    @Column(name = "placed_at", nullable = false)
    private Instant placedAt;

    protected OrderJpaEntity() { }

    public OrderJpaEntity(UUID id, UUID checkoutId, String status, List<LineJson> lines, Instant placedAt) {
        this.id = id; this.checkoutId = checkoutId; this.status = status;
        this.lines = lines; this.placedAt = placedAt;
    }

    public UUID getId() { return id; }
    public UUID getCheckoutId() { return checkoutId; }
    public String getStatus() { return status; }
    public List<LineJson> getLines() { return lines; }
    public Instant getPlacedAt() { return placedAt; }

}