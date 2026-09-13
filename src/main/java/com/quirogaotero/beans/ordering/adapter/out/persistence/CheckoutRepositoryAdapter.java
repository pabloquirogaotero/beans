package com.quirogaotero.beans.ordering.adapter.out.persistence;

import com.quirogaotero.beans.inventory.domain.CheckoutId;
import com.quirogaotero.beans.ordering.application.port.out.CheckoutRepository;
import com.quirogaotero.beans.ordering.domain.Checkout;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CheckoutRepositoryAdapter implements CheckoutRepository {

    private final CheckoutJpaRepository jpa;
    public CheckoutRepositoryAdapter(CheckoutJpaRepository jpa) { this.jpa = jpa; }

    @Override public void save(Checkout checkout) { jpa.save(CheckoutMapper.toEntity(checkout)); }
    @Override public Optional<Checkout> findById(CheckoutId id) {
        return jpa.findById(id.value()).map(CheckoutMapper::toDomain);
    }

}