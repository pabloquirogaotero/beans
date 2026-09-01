package com.quirogaotero.beans.inventory.application.service;

import com.quirogaotero.beans.inventory.application.port.in.ExpireReservationsUseCase;
import com.quirogaotero.beans.inventory.application.port.out.LotStockRepository;
import com.quirogaotero.beans.inventory.application.port.out.ReservationRepository;
import com.quirogaotero.beans.inventory.domain.LotStock;
import com.quirogaotero.beans.inventory.domain.Reservation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

public class ExpireReservationsService implements ExpireReservationsUseCase {

  private final ReservationRepository reservations;
  private final LotStockRepository lotStocks;
  private final Clock clock;

  public ExpireReservationsService(ReservationRepository reservations,
                                   LotStockRepository lotStocks, Clock clock) {
    this.reservations = reservations;
    this.lotStocks = lotStocks;
    this.clock = clock;
  }

  @Override
  @Transactional
  public int expireReservations() {
    List<Reservation> expired = reservations.findExpired(clock.instant());
    for (Reservation reservation : expired) {
      // give the stock back
      LotStock stock = lotStocks.find(reservation.getLotStockId())
              .orElseThrow(() -> new IllegalStateException(
                      "LotStock missing for expired reservation: " + reservation.getId()));
      stock.release(reservation.getQuantity());
      reservation.release();
      lotStocks.save(stock);
      reservations.save(reservation);
    }
    return expired.size();
  }
}