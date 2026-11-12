package com.quirogaotero.beans.inventory.application.service;

import com.quirogaotero.beans.inventory.application.port.in.ExpireReservationsUseCase;
import com.quirogaotero.beans.inventory.application.port.out.LotStockRepository;
import com.quirogaotero.beans.inventory.application.port.out.ReservationRepository;
import com.quirogaotero.beans.inventory.application.port.out.StockLedger;
import com.quirogaotero.beans.inventory.domain.LotStock;
import com.quirogaotero.beans.inventory.domain.Reservation;
import com.quirogaotero.beans.inventory.domain.StockLedgerEntry;
import com.quirogaotero.beans.inventory.domain.StockMovement;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

public class ExpireReservationsService implements ExpireReservationsUseCase {

  private final ReservationRepository reservations;
  private final LotStockRepository lotStocks;
  private final StockLedger ledger;
  private final Clock clock;

  public ExpireReservationsService(ReservationRepository reservations,
                                   LotStockRepository lotStocks, StockLedger ledger, Clock clock) {
    this.reservations = reservations;
    this.lotStocks = lotStocks;
    this.ledger = ledger;
    this.clock = clock;
  }

  @Override
  @Transactional
  public int expireReservations() {
    Instant now = clock.instant();
    List<Reservation> expired = reservations.findExpired(now);
    for (Reservation reservation : expired) {
      // give the stock back
      LotStock stock = lotStocks.find(reservation.getLotStockId())
              .orElseThrow(() -> new IllegalStateException(
                      "LotStock missing for expired reservation: " + reservation.getId()));
      StockMovement movement = stock.release(reservation.getQuantity());
      reservation.release();
      lotStocks.save(stock);
      reservations.save(reservation);
      ledger.record(StockLedgerEntry.forCheckout(movement, reservation.getCheckoutId(), now));
    }
    return expired.size();
  }
}