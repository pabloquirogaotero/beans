package com.quirogaotero.beans.inventory.adapter.out.persistence;

import com.quirogaotero.beans.inventory.application.port.out.ReservationRepository;
import com.quirogaotero.beans.inventory.domain.Reservation;
import org.springframework.stereotype.Repository;

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

}
