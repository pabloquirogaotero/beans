package com.quirogaotero.beans.ordering.application;

import com.quirogaotero.beans.inventory.application.port.in.ReservationResult;
import com.quirogaotero.beans.inventory.domain.CheckoutId;

public record CheckoutOutcome(CheckoutId checkoutId, ReservationResult result) {}
