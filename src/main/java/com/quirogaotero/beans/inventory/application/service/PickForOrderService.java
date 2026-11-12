package com.quirogaotero.beans.inventory.application.service;

import com.quirogaotero.beans.inventory.application.port.in.PickForOrderCommand;
import com.quirogaotero.beans.inventory.application.port.in.PickForOrderUseCase;
import com.quirogaotero.beans.inventory.application.port.out.InventoryOutbox;
import com.quirogaotero.beans.inventory.application.port.out.LotStockRepository;
import com.quirogaotero.beans.inventory.application.port.out.ReservationRepository;
import com.quirogaotero.beans.inventory.application.port.out.StockLedger;
import com.quirogaotero.beans.inventory.domain.*;
import com.quirogaotero.beans.messaging.application.port.out.ProcessedEvents;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PickForOrderService implements PickForOrderUseCase {

  static final String CONSUMER = "inventory.pick-for-order";

  private final ReservationRepository reservations;
  private final LotStockRepository lotStocks;
  private final StockLedger ledger;
  private final ProcessedEvents processedEvents;
  private final InventoryOutbox outbox;
  private final Clock clock;

  public PickForOrderService(ReservationRepository reservations, LotStockRepository lotStocks, StockLedger ledger,
                             ProcessedEvents processedEvents, InventoryOutbox outbox, Clock clock) {
    this.reservations = reservations;
    this.lotStocks = lotStocks;
    this.ledger = ledger;
    this.processedEvents = processedEvents;
    this.outbox = outbox;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void pickForOrder(PickForOrderCommand command) {
    // 1. Events arrive at least once: a repeat must not take the stock a second time.
    if (!processedEvents.markProcessed(CONSUMER, command.eventId()))
      return;

    // 2. Pick every confirmed reservation of the checkout the order came from.
    List<Reservation> confirmed = reservations.findByCheckoutId(command.checkoutId()).stream()
            .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
            .toList();
    if (confirmed.isEmpty())
      throw new IllegalStateException(
              "No confirmed reservations to pick for checkout " + command.checkoutId());

    Instant now = clock.instant();
    List<StockPicked.Line> picked = new ArrayList<>();
    for (Reservation reservation : confirmed) {
      LotStock stock = lotStocks.find(reservation.getLotStockId())
              .orElseThrow(() -> new IllegalStateException(
                      "LotStock missing for reservation: " + reservation.getId()));
      StockMovement movement = stock.pick(reservation.getQuantity());
      reservation.pick();
      lotStocks.save(stock);
      reservations.save(reservation);
      // The ledger line that a recall of this lot will find: these units went to this order.
      ledger.record(StockLedgerEntry.forOrder(movement, command.checkoutId(), command.orderId(), now));
      picked.add(new StockPicked.Line(reservation.getLotStockId(), reservation.getQuantity()));
    }

    // 3. Announce it in this same transaction, so the stock change and the news of it agree.
    outbox.append(new StockPicked(UUID.randomUUID(), command.orderId(), command.checkoutId(),
            picked, now));
  }

}
