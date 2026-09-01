package com.quirogaotero.beans.inventory.application.port.in;

public interface ReserveForCheckoutUseCase {

  ReservationResult reserveForCheckout(ReserveForCheckoutCommand command);

}
