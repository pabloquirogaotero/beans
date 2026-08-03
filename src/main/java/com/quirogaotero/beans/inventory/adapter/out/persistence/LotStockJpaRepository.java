package com.quirogaotero.beans.inventory.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LotStockJpaRepository
        extends JpaRepository<LotStockJpaEntity, LotStockKey> {
}
