package com.quirogaotero.beans.inventory.application.service;

import com.quirogaotero.beans.inventory.application.port.in.PickForOrderCommand;
import com.quirogaotero.beans.inventory.application.port.out.InMemoryInventoryOutbox;
import com.quirogaotero.beans.inventory.application.port.out.InMemoryLotStockRepository;
import com.quirogaotero.beans.inventory.application.port.out.InMemoryReservationRepository;
import com.quirogaotero.beans.inventory.application.port.out.InMemoryStockLedger;
import com.quirogaotero.beans.inventory.domain.*;
import com.quirogaotero.beans.messaging.application.port.out.InMemoryProcessedEvents;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PickForOrderServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T10:00:00Z"), ZoneOffset.UTC);
    private final InMemoryReservationRepository reservations = new InMemoryReservationRepository();
    private final InMemoryLotStockRepository lotStocks = new InMemoryLotStockRepository();
    private final InMemoryInventoryOutbox outbox = new InMemoryInventoryOutbox();
    private final InMemoryStockLedger ledger = new InMemoryStockLedger();
    private final PickForOrderService service = new PickForOrderService(
            reservations, lotStocks, ledger, new InMemoryProcessedEvents(), outbox, clock);

    private final CheckoutId checkout = CheckoutId.newCheckoutId();
    private final UUID orderId = UUID.randomUUID();

    /** A lot with 10 on hand, of which {@code quantity} is held by a reservation of the checkout. */
    private LotStockId lotWithReservation(int quantity, boolean confirmed) {
        LotStockId id = new LotStockId(new LocationId("WRH-OUR-01"), LotId.newLotId());
        lotStocks.seed(new LotStock(id, Quantity.of(10), Quantity.of(quantity), 0L));
        Reservation reservation = Reservation.newActiveReservation(
                checkout, id, Quantity.of(quantity), clock.instant().plusSeconds(900));
        if (confirmed) reservation.confirm(clock.instant());
        reservations.save(reservation);
        return id;
    }

    private PickForOrderCommand command(UUID eventId) {
        return new PickForOrderCommand(eventId, orderId, checkout);
    }

    @Test
    void picks_the_confirmed_stock_out_of_both_on_hand_and_reserved() {
        LotStockId lot = lotWithReservation(3, true);

        service.pickForOrder(command(UUID.randomUUID()));

        LotStock stock = lotStocks.find(lot).orElseThrow();
        assertEquals(7, stock.getOnHand().value());
        assertEquals(0, stock.getReserved().value());
        assertTrue(reservations.findByCheckoutId(checkout).stream()
                .allMatch(r -> r.getStatus() == ReservationStatus.PICKED));
    }

    @Test
    void announces_what_was_picked_for_the_order() {
        LotStockId first = lotWithReservation(3, true);
        LotStockId second = lotWithReservation(2, true);

        service.pickForOrder(command(UUID.randomUUID()));

        assertEquals(1, outbox.events.size());
        StockPicked event = outbox.events.getFirst();
        assertEquals(orderId, event.orderId());
        assertEquals(checkout, event.checkoutId());
        assertEquals(clock.instant(), event.occurredAt());
        assertEquals(2, event.lines().size());
        assertTrue(event.lines().contains(new StockPicked.Line(first, Quantity.of(3))));
        assertTrue(event.lines().contains(new StockPicked.Line(second, Quantity.of(2))));
    }

    @Test
    void records_each_pick_in_the_ledger_against_the_order() {
        LotStockId lot = lotWithReservation(3, true);

        service.pickForOrder(command(UUID.randomUUID()));

        assertEquals(1, ledger.entries.size());
        StockLedgerEntry entry = ledger.entries.getFirst();
        assertEquals(StockMovementType.PICK, entry.type());
        assertEquals(lot, entry.lotStockId());
        assertEquals(Quantity.of(3), entry.quantity());
        assertEquals(orderId, entry.orderId());
        assertEquals(checkout, entry.checkoutId());
    }

    @Test
    void a_repeated_event_does_not_pick_the_stock_twice() {
        LotStockId lot = lotWithReservation(3, true);
        UUID eventId = UUID.randomUUID();

        service.pickForOrder(command(eventId));
        service.pickForOrder(command(eventId));

        assertEquals(7, lotStocks.find(lot).orElseThrow().getOnHand().value());
        assertEquals(1, outbox.events.size());
        assertEquals(1, ledger.entries.size());
    }

    @Test
    void fails_when_the_checkout_has_nothing_confirmed_to_pick() {
        LotStockId lot = lotWithReservation(3, false);

        assertThrows(IllegalStateException.class, () -> service.pickForOrder(command(UUID.randomUUID())));

        assertEquals(10, lotStocks.find(lot).orElseThrow().getOnHand().value());
        assertTrue(outbox.events.isEmpty());
    }

}
