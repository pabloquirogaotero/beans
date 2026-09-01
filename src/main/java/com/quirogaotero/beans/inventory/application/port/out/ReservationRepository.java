package com.quirogaotero.beans.inventory.application.port.out;

import com.quirogaotero.beans.inventory.domain.Reservation;
import com.quirogaotero.beans.inventory.domain.ReservationId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

  void save(Reservation reservation);

  List<Reservation> findExpired(Instant now);

  Optional<Reservation> findById(ReservationId id);
}
