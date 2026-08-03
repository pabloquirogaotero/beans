package com.quirogaotero.beans.ordering.application;

import com.quirogaotero.beans.inventory.application.port.in.*;
import com.quirogaotero.beans.inventory.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StartCheckoutService {

  private final ReserveForCheckoutUseCase reserveForCheckout;

  public StartCheckoutService(ReserveForCheckoutUseCase reserveForCheckout) {
    this.reserveForCheckout = reserveForCheckout;
  }

  public CheckoutOutcome startCheckout(StartCheckoutCommand command) {
    CheckoutId checkoutId = CheckoutId.newCheckoutId();

    List<RequestedLine> lines = command.lines().stream()
            .map(l -> new RequestedLine(new SkuId(l.sku()), Quantity.of(l.quantity())))
            .toList();
    Coordinates destination = new Coordinates(command.latitude(), command.longitude());

    ReservationResult result = reserveForCheckout.reserveForCheckout(
            new ReserveForCheckoutCommand(checkoutId, destination, lines));

    return new CheckoutOutcome(checkoutId, result);
  }

}
