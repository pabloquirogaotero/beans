package com.quirogaotero.beans.inventory.adapter.out.persistence;

import com.quirogaotero.beans.inventory.application.port.out.ReservationRepository;
import com.quirogaotero.beans.inventory.domain.Reservation;
import com.quirogaotero.beans.inventory.domain.ReservationId;
import com.quirogaotero.beans.inventory.domain.ReservationStatus;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationRepositoryAdapter implements ReservationRepository {

  private final ReservationJpaRepository jpa;

  public ReservationRepositoryAdapter(ReservationJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public void save(Reservation reservation) {
    jpa.findById(reservation.getId().value()).ifPresentOrElse(
            existing -> ReservationMapper.applyTo(reservation, existing),
            ()       -> jpa.save(ReservationMapper.toEntity(reservation))
    );
  }

  @Override
  public List<Reservation> findExpired(Instant now) {
    return jpa.findByStatusAndExpiresAtBefore(ReservationStatus.ACTIVE, now)
            .stream().map(ReservationMapper::toDomain).toList();
  }

  @Override
  public Optional<Reservation> findById(ReservationId id) {
    return jpa.findById(id.value()).map(ReservationMapper::toDomain);
  }

}
