package com.quirogaotero.beans.ordering.application;

import com.quirogaotero.beans.inventory.application.port.in.ReservationResult;
import com.quirogaotero.beans.inventory.application.port.in.ReserveForCheckoutUseCase;
import com.quirogaotero.beans.inventory.domain.CheckoutId;
import com.quirogaotero.beans.inventory.domain.ReservationId;
import com.quirogaotero.beans.ordering.domain.CheckoutStatus;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StartCheckoutServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T10:00:00Z"), ZoneOffset.UTC);
    private final InMemoryCheckoutRepository checkouts = new InMemoryCheckoutRepository();

    private final StartCheckoutCommand command = new StartCheckoutCommand(42.34, -7.86,
            List.of(new StartCheckoutCommand.Line("COF-COLHU-WBN-250", 2)));

    private ReserveForCheckoutUseCase reserveThatSucceeds() {
        return cmd -> ReservationResult.newSuccessfulReservation(
                List.of(ReservationId.newReservationId()), clock.instant().plusSeconds(900));
    }
    private ReserveForCheckoutUseCase reserveThatFails() {
        return cmd -> ReservationResult.newFailedReservation("Insufficient stock");
    }

    private StartCheckoutService service(ReserveForCheckoutUseCase reserve) {
        return new StartCheckoutService(reserve, checkouts, clock);
    }

    @Test
    void persists_a_started_checkout_when_stock_is_reserved() {
        CheckoutOutcome outcome = service(reserveThatSucceeds()).startCheckout(command);

        assertTrue(outcome.result().reserved());
        var stored = checkouts.findById(outcome.checkoutId()).orElseThrow();
        assertEquals(CheckoutStatus.STARTED, stored.getStatus());
        assertEquals(2, stored.getLines().getFirst().quantity());
    }

    @Test
    void does_not_persist_a_checkout_when_the_reservation_fails() {
        CheckoutOutcome outcome = service(reserveThatFails()).startCheckout(command);

        assertFalse(outcome.result().reserved());
        assertTrue(checkouts.store.isEmpty());
    }

    @Test
    void reserves_under_the_id_of_the_checkout_it_returns() {
        var seen = new ArrayList<CheckoutId>();
        ReserveForCheckoutUseCase recording = cmd -> {
            seen.add(cmd.checkoutId());
            return reserveThatSucceeds().reserveForCheckout(cmd);
        };

        CheckoutOutcome outcome = service(recording).startCheckout(command);

        assertEquals(List.of(outcome.checkoutId()), seen);
    }

}
