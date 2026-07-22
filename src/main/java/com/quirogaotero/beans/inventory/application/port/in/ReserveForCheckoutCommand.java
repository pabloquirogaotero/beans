package com.quirogaotero.beans.inventory.application.port.in;

import com.quirogaotero.beans.inventory.domain.CheckoutId;
import com.quirogaotero.beans.inventory.domain.Coordinates;
import com.quirogaotero.beans.inventory.domain.RequestedLine;

import java.util.List;
import java.util.Objects;

public record ReserveForCheckoutCommand(CheckoutId checkoutId, Coordinates destination, List<RequestedLine> lines) {

  public ReserveForCheckoutCommand {
    Objects.requireNonNull(checkoutId, "CheckoutId must not be null.");
    Objects.requireNonNull(destination, "Destination must not be null.");
    if (lines == null || lines.isEmpty())
      throw new IllegalArgumentException("Lines must not be null.");
  }

}
