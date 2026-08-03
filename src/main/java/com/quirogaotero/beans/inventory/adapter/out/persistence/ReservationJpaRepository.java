package com.quirogaotero.beans.inventory.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ReservationJpaRepository
        extends JpaRepository<ReservationJpaEntity, UUID> {
}