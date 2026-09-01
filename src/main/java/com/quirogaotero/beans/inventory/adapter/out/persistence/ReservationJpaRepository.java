package com.quirogaotero.beans.inventory.adapter.out.persistence;

import com.quirogaotero.beans.inventory.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReservationJpaRepository
        extends JpaRepository<ReservationJpaEntity, UUID> {

  List<ReservationJpaEntity> findByStatusAndExpiresAtBefore(ReservationStatus status, Instant now);

}