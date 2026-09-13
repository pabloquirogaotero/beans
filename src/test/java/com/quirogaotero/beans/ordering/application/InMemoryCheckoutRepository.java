package com.quirogaotero.beans.ordering.application;

import com.quirogaotero.beans.inventory.domain.CheckoutId;
import com.quirogaotero.beans.ordering.application.port.out.CheckoutRepository;
import com.quirogaotero.beans.ordering.domain.Checkout;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class InMemoryCheckoutRepository implements CheckoutRepository {

    final Map<CheckoutId, Checkout> store = new HashMap<>();

    public void save(Checkout checkout) { store.put(checkout.getId(), checkout); }

    public Optional<Checkout> findById(CheckoutId id) { return Optional.ofNullable(store.get(id)); }

}
