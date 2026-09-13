package com.quirogaotero.beans.inventory.application.service;

import com.quirogaotero.beans.inventory.application.port.in.ConfirmReservationsUseCase;
import com.quirogaotero.beans.inventory.application.port.in.ReservationsNotConfirmableException;
import com.quirogaotero.beans.inventory.application.port.out.ReservationRepository;
import com.quirogaotero.beans.inventory.domain.CheckoutId;
import com.quirogaotero.beans.inventory.domain.Reservation;
import com.quirogaotero.beans.inventory.domain.ReservationStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

public class ConfirmReservationsService implements ConfirmReservationsUseCase {

    private final ReservationRepository reservations;
    private final Clock clock;

    public ConfirmReservationsService(ReservationRepository reservations, Clock clock) {
        this.reservations = reservations;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void confirmReservations(CheckoutId checkoutId) {
        Instant now = clock.instant();
        List<Reservation> checkoutReservations = reservations.findByCheckoutId(checkoutId);

        if (checkoutReservations.isEmpty())
            throw new ReservationsNotConfirmableException(checkoutId);

        for (Reservation reservation : checkoutReservations) {
            if (reservation.getStatus() != ReservationStatus.ACTIVE || reservation.isExpired(now))
                throw new ReservationsNotConfirmableException(checkoutId);
        }

        for (Reservation reservation : checkoutReservations) {
            reservation.confirm(now);
            reservations.save(reservation);
        }
    }

}
