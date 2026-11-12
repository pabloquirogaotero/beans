package com.quirogaotero.beans.ordering.application;

import com.quirogaotero.beans.inventory.application.port.in.ConfirmReservationsUseCase;
import com.quirogaotero.beans.inventory.application.port.in.ReservationsNotConfirmableException;
import com.quirogaotero.beans.inventory.domain.CheckoutId;
import com.quirogaotero.beans.ordering.application.port.out.*;
import com.quirogaotero.beans.ordering.domain.*;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PayCheckoutServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T10:00:00Z"), ZoneOffset.UTC);
    private final InMemoryCheckoutRepository checkouts = new InMemoryCheckoutRepository();
    private final InMemoryOrderRepository orders = new InMemoryOrderRepository();
    private final InMemoryOrderOutbox outbox = new InMemoryOrderOutbox();

    private final List<OrderLine> lines = List.of(new OrderLine("COF-COLHU-WBN-250", 2));

    private ConfirmReservationsUseCase confirmThatSucceeds() {
        return checkoutId -> { /* success: no-op */ };
    }
    private ConfirmReservationsUseCase confirmThatFails() {
        return checkoutId -> { throw new ReservationsNotConfirmableException(checkoutId); };
    }

    private final List<Money> charged = new ArrayList<>();

    private PaymentPort paymentApproves() {
        return (checkoutId, amount) -> {
            charged.add(amount);
            return PaymentResult.approved("FAKE-REF");
        };
    }
    private PaymentPort paymentDeclines() {
        return (checkoutId, amount) -> PaymentResult.declined("insufficient funds");
    }

    private PayCheckoutService service(ConfirmReservationsUseCase confirm, PaymentPort payment) {
        return new PayCheckoutService(checkouts, orders, outbox, confirm, payment, clock);
    }

    private Checkout seededStartedCheckout() {
        Checkout checkout = Checkout.start(lines, Money.euros(2500), clock.instant());
        checkouts.save(checkout);
        return checkout;
    }

    @Test
    void pays_successfully_creates_order_and_marks_checkout_paid() {
        Checkout checkout = seededStartedCheckout();

        PayCheckoutResult result = service(confirmThatSucceeds(), paymentApproves())
                .pay(checkout.getId());

        assertTrue(result.paid());
        assertNotNull(result.orderId());
        assertEquals(Money.euros(2500), result.charged());
        // an order was stored
        assertTrue(orders.findById(result.orderId()).isPresent());
        // the checkout is now PAID
        assertEquals(CheckoutStatus.PAID,
                checkouts.findById(checkout.getId()).orElseThrow().getStatus());
    }

    @Test
    void charges_the_total_the_checkout_was_priced_at() {
        Checkout checkout = seededStartedCheckout();

        PayCheckoutResult result = service(confirmThatSucceeds(), paymentApproves()).pay(checkout.getId());

        assertEquals(List.of(checkout.getTotal()), charged);
        assertEquals(checkout.getTotal(), orders.findById(result.orderId()).orElseThrow().getTotal());
    }

    @Test
    void records_one_order_placed_event_for_the_new_order() {
        Checkout checkout = seededStartedCheckout();

        PayCheckoutResult result = service(confirmThatSucceeds(), paymentApproves())
                .pay(checkout.getId());

        assertEquals(1, outbox.events.size());
        OrderPlaced event = outbox.events.getFirst();
        assertEquals(result.orderId(), event.orderId());
        assertEquals(checkout.getId(), event.checkoutId());
        assertEquals(lines, event.lines());
    }

    @Test
    void records_no_event_when_no_order_is_created() {
        Checkout expired = seededStartedCheckout();
        service(confirmThatFails(), paymentApproves()).pay(expired.getId());

        Checkout declined = seededStartedCheckout();
        assertThrows(PaymentDeclinedException.class,
                () -> service(confirmThatSucceeds(), paymentDeclines())
                        .pay(declined.getId()));

        assertTrue(outbox.events.isEmpty());
    }

    @Test
    void returns_reservation_expired_when_confirmation_fails() {
        Checkout checkout = seededStartedCheckout();

        PayCheckoutResult result = service(confirmThatFails(), paymentApproves())
                .pay(checkout.getId());

        assertFalse(result.paid());
        assertEquals("Reservation expired", result.reason());

        assertEquals(CheckoutStatus.STARTED,
                checkouts.findById(checkout.getId()).orElseThrow().getStatus());
    }

    @Test
    void throws_when_payment_is_declined() {
        Checkout checkout = seededStartedCheckout();

        assertThrows(PaymentDeclinedException.class,
                () -> service(confirmThatSucceeds(), paymentDeclines())
                        .pay(checkout.getId()));
    }

    @Test
    void throws_when_checkout_does_not_exist() {
        assertThrows(CheckoutNotFoundException.class,
                () -> service(confirmThatSucceeds(), paymentApproves())
                        .pay(CheckoutId.newCheckoutId()));
    }

    @Test
    void cannot_pay_the_same_checkout_twice() {
        Checkout checkout = seededStartedCheckout();
        var svc = service(confirmThatSucceeds(), paymentApproves());
        svc.pay(checkout.getId());

        assertThrows(CheckoutNotPayableException.class,
                () -> svc.pay(checkout.getId()));
    }

}