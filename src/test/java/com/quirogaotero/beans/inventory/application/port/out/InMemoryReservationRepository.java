package com.quirogaotero.beans.inventory.application.port.out;

import com.quirogaotero.beans.inventory.domain.Reservation;

import java.util.ArrayList;
import java.util.List;

public class InMemoryReservationRepository implements ReservationRepository {

  public final List<Reservation> saved = new ArrayList<>();

  public void save(Reservation r) { saved.add(r); }

}
