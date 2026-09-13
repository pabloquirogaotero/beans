package com.quirogaotero.beans.ordering.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CheckoutJpaRepository extends JpaRepository<CheckoutJpaEntity, UUID> { }