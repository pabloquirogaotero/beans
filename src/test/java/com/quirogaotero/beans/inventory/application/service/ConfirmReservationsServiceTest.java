package com.quirogaotero.beans.inventory.application.service;

import com.quirogaotero.beans.inventory.application.port.in.ReservationsNotConfirmableException;
import com.quirogaotero.beans.inventory.application.port.out.InMemoryReservationRepository;
import com.quirogaotero.beans.inventory.domain.*;
import org.junit.jupiter.api.Test;

import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class ConfirmReservationsServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T10:00:00Z"), ZoneOffset.UTC);
    private final InMemoryReservationRepository reservations = new InMemoryReservationRepository();
    private final ConfirmReservationsService service = new ConfirmReservationsService(reservations, clock);

    private final LotStockId lotStockId =
            new LotStockId(new LocationId("WRH-OUR-01"), new LotId(java.util.UUID.randomUUID()));

    private Reservation activeFor(CheckoutId checkoutId, Instant expiresAt) {
        return Reservation.newActiveReservation(checkoutId, lotStockId, Quantity.of(1), expiresAt);
    }

    @Test
    void confirms_all_active_reservations_of_the_checkout() {
        var checkout = CheckoutId.newCheckoutId();
        reservations.save(activeFor(checkout, clock.instant().plusSeconds(900)));
        reservations.save(activeFor(checkout, clock.instant().plusSeconds(900)));

        service.confirmReservations(checkout);

        assertTrue(reservations.findByCheckoutId(checkout).stream()
                .allMatch(r -> r.getStatus() == ReservationStatus.CONFIRMED));
    }

    @Test
    void fails_when_a_reservation_has_expired() {
        var checkout = CheckoutId.newCheckoutId();
        reservations.save(activeFor(checkout, clock.instant().plusSeconds(900)));
        reservations.save(activeFor(checkout, clock.instant().minusSeconds(1)));

        assertThrows(ReservationsNotConfirmableException.class,
                () -> service.confirmReservations(checkout));

        assertTrue(reservations.findByCheckoutId(checkout).stream()
                .allMatch(r -> r.getStatus() == ReservationStatus.ACTIVE));
    }

    @Test
    void fails_when_the_checkout_has_no_reservations() {
        assertThrows(ReservationsNotConfirmableException.class,
                () -> service.confirmReservations(CheckoutId.newCheckoutId()));
    }

}
