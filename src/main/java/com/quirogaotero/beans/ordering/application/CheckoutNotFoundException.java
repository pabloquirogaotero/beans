package com.quirogaotero.beans.ordering.application;

import com.quirogaotero.beans.inventory.domain.CheckoutId;

public class CheckoutNotFoundException extends RuntimeException {

    public CheckoutNotFoundException(CheckoutId id) { super("Checkout not found: " + id); }

}
