package com.quirogaotero.beans.ordering.application;

import com.quirogaotero.beans.inventory.application.port.in.ConfirmReservationsUseCase;
import com.quirogaotero.beans.inventory.application.port.in.ReservationsNotConfirmableException;
import com.quirogaotero.beans.inventory.domain.CheckoutId;
import com.quirogaotero.beans.ordering.application.port.out.CheckoutRepository;
import com.quirogaotero.beans.ordering.application.port.out.OrderRepository;
import com.quirogaotero.beans.ordering.application.port.out.PaymentPort;
import com.quirogaotero.beans.ordering.application.port.out.PaymentResult;
import com.quirogaotero.beans.ordering.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class PayCheckoutService {

    private final CheckoutRepository checkouts;
    private final OrderRepository orders;
    private final ConfirmReservationsUseCase confirmReservations;
    private final PaymentPort payment;
    private final Clock clock;

    public PayCheckoutService(CheckoutRepository checkouts, OrderRepository orders,
                              ConfirmReservationsUseCase confirmReservations,
                              PaymentPort payment, Clock clock) {
        this.checkouts = checkouts;
        this.orders = orders;
        this.confirmReservations = confirmReservations;
        this.payment = payment;
        this.clock = clock;
    }

    @Transactional
    public PayCheckoutResult pay(CheckoutId checkoutId, Money amount) {
        // 1. Load the checkout; it must exist and still be payable.
        Checkout checkout = checkouts.findById(checkoutId)
                .orElseThrow(() -> new CheckoutNotFoundException(checkoutId));

        if (checkout.getStatus() != CheckoutStatus.STARTED)
            throw new CheckoutNotPayableException(checkoutId, checkout.getStatus());

        // 2. Confirm the reservations FIRST — if they expired, we can't fulfil, so we don't charge.
        try {
            confirmReservations.confirmReservations(checkoutId);
        } catch (ReservationsNotConfirmableException expired) {
            return PayCheckoutResult.reservationExpired();
        }

        // 3. Charge (simulated). A decline is a normal business outcome, not an exception.
        PaymentResult paymentResult = payment.charge(checkoutId, amount);
        if (!paymentResult.approved()) {
            // The transaction rolls back the confirmation via the exception below.
            throw new PaymentDeclinedException(checkoutId, paymentResult.declineReason());
        }

        // 4. Create the order from the checkout's lines (Ordering owns them — no need to ask Inventory).
        Order order = Order.place(checkoutId, checkout.getLines(), clock.instant());
        orders.save(order);

        // 5. Mark the checkout paid (guards against double payment).
        checkout.markPaid();
        checkouts.save(checkout);

        return PayCheckoutResult.paid(order.getId(), paymentResult.reference());
    }

}