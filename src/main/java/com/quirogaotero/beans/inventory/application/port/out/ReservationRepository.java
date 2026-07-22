package com.quirogaotero.beans.inventory.application.port.out;

import com.quirogaotero.beans.inventory.domain.Reservation;

public interface ReservationRepository {

  void save(Reservation reservation);

}
