package com.quirogaotero.beans.ordering.application.port.out;

import com.quirogaotero.beans.inventory.domain.CheckoutId;
import com.quirogaotero.beans.ordering.domain.Checkout;

import java.util.Optional;

public interface CheckoutRepository {

    void save(Checkout checkout);
    Optional<Checkout> findById(CheckoutId id);

}
