package com.quirogaotero.beans.ordering.application;

import com.quirogaotero.beans.inventory.application.port.in.ReservationResult;
import com.quirogaotero.beans.inventory.domain.CheckoutId;
import com.quirogaotero.beans.ordering.domain.Money;

public record CheckoutOutcome(CheckoutId checkoutId, Money total, ReservationResult result) {}
