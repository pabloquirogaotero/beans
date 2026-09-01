package com.quirogaotero.beans.inventory.application.port.out;

import com.quirogaotero.beans.inventory.domain.Reservation;
import com.quirogaotero.beans.inventory.domain.ReservationId;
import com.quirogaotero.beans.inventory.domain.ReservationStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryReservationRepository implements ReservationRepository {

  public final List<Reservation> saved = new ArrayList<>();

  public void save(Reservation r) { saved.add(r); }

  @Override
  public List<Reservation> findExpired(Instant now) {
    return saved.stream()
            .filter(r -> r.getStatus() == ReservationStatus.ACTIVE)
            .filter(r -> r.getExpiresAt().isBefore(now))
            .toList();
  }

  @Override
  public Optional<Reservation> findById(ReservationId id) {
    return saved.stream().filter(r -> r.getId().equals(id)).findFirst();
  }

}
