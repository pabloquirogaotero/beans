package com.quirogaotero.beans.ordering.application;

import com.quirogaotero.beans.inventory.domain.CheckoutId;
import com.quirogaotero.beans.ordering.domain.CheckoutStatus;

public class CheckoutNotPayableException extends RuntimeException {

    public CheckoutNotPayableException(CheckoutId checkoutId, CheckoutStatus status) {
        super("Checkout " + checkoutId + " is not payable (status: " + status + ").");
    }

}