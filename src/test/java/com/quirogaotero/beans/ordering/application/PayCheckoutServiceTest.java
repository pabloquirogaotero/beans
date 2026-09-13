package com.quirogaotero.beans.ordering.application;

import com.quirogaotero.beans.inventory.application.port.in.ConfirmReservationsUseCase;
import com.quirogaotero.beans.inventory.application.port.in.ReservationsNotConfirmableException;
import com.quirogaotero.beans.inventory.domain.CheckoutId;
import com.quirogaotero.beans.ordering.application.port.out.*;
import com.quirogaotero.beans.ordering.domain.*;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PayCheckoutServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T10:00:00Z"), ZoneOffset.UTC);
    private final InMemoryCheckoutRepository checkouts = new InMemoryCheckoutRepository();
    private final InMemoryOrderRepository orders = new InMemoryOrderRepository();

    private final List<OrderLine> lines = List.of(new OrderLine("COF-COLHU-WBN-250", 2));

    private ConfirmReservationsUseCase confirmThatSucceeds() {
        return checkoutId -> { /* success: no-op */ };
    }
    private ConfirmReservationsUseCase confirmThatFails() {
        return checkoutId -> { throw new ReservationsNotConfirmableException(checkoutId); };
    }

    private PaymentPort paymentApproves() {
        return (checkoutId, amount) -> PaymentResult.approved("FAKE-REF");
    }
    private PaymentPort paymentDeclines() {
        return (checkoutId, amount) -> PaymentResult.declined("insufficient funds");
    }

    private PayCheckoutService service(ConfirmReservationsUseCase confirm, PaymentPort payment) {
        return new PayCheckoutService(checkouts, orders, confirm, payment, clock);
    }

    private Checkout seededStartedCheckout() {
        Checkout checkout = Checkout.start(lines, clock.instant());
        checkouts.save(checkout);
        return checkout;
    }

    @Test
    void pays_successfully_creates_order_and_marks_checkout_paid() {
        Checkout checkout = seededStartedCheckout();

        PayCheckoutResult result = service(confirmThatSucceeds(), paymentApproves())
                .pay(checkout.getId(), Money.euros(2400));

        assertTrue(result.paid());
        assertNotNull(result.orderId());
        // an order was stored
        assertTrue(orders.findById(result.orderId()).isPresent());
        // the checkout is now PAID
        assertEquals(CheckoutStatus.PAID,
                checkouts.findById(checkout.getId()).orElseThrow().getStatus());
    }

    @Test
    void returns_reservation_expired_when_confirmation_fails() {
        Checkout checkout = seededStartedCheckout();

        PayCheckoutResult result = service(confirmThatFails(), paymentApproves())
                .pay(checkout.getId(), Money.euros(2400));

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
                        .pay(checkout.getId(), Money.euros(2400)));
    }

    @Test
    void throws_when_checkout_does_not_exist() {
        assertThrows(CheckoutNotFoundException.class,
                () -> service(confirmThatSucceeds(), paymentApproves())
                        .pay(CheckoutId.newCheckoutId(), Money.euros(2400)));
    }

    @Test
    void cannot_pay_the_same_checkout_twice() {
        Checkout checkout = seededStartedCheckout();
        var svc = service(confirmThatSucceeds(), paymentApproves());
        svc.pay(checkout.getId(), Money.euros(2400));

        assertThrows(IllegalStateException.class,
                () -> svc.pay(checkout.getId(), Money.euros(2400)));
    }

}