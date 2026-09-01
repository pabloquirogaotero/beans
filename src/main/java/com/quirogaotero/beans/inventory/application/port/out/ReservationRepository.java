package com.quirogaotero.beans.inventory.application.port.out;

import com.quirogaotero.beans.inventory.domain.Reservation;

import java.time.Instant;
import java.util.List;

public interface ReservationRepository {

  void save(Reservation reservation);

  List<Reservation> findExpired(Instant now);

}
