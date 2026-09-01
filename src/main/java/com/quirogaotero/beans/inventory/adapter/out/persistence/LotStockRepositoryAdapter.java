package com.quirogaotero.beans.inventory.adapter.out.persistence;

import com.quirogaotero.beans.inventory.application.port.out.LotStockRepository;
import com.quirogaotero.beans.inventory.domain.LotStock;
import com.quirogaotero.beans.inventory.domain.LotStockId;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class LotStockRepositoryAdapter implements LotStockRepository {

  private final LotStockJpaRepository jpa;

  public LotStockRepositoryAdapter(LotStockJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Optional<LotStock> find(LotStockId id) {
    return jpa.findById(LotStockMapper.toKey(id)).map(LotStockMapper::toDomain);
  }

  @Override
  public void save(LotStock lotStock) {
    LotStockJpaEntity entity = LotStockMapper.toEntity(lotStock);
    jpa.save(entity);
  }

}
