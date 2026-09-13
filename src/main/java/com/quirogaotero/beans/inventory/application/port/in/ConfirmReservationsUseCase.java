package com.quirogaotero.beans.inventory.application.port.in;

import com.quirogaotero.beans.inventory.domain.CheckoutId;

public interface ConfirmReservationsUseCase {

    void confirmReservations(CheckoutId checkoutId);

}