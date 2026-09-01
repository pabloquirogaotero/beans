package com.quirogaotero.beans.inventory.adapter.out.persistence;

import com.quirogaotero.beans.inventory.InventoryFixture;
import com.quirogaotero.beans.inventory.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.quirogaotero.beans.TestcontainersConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({TestcontainersConfiguration.class, InventoryFixture.class})
class LotStockVersionIT {

  @Autowired LotStockRepositoryAdapter repository;
  @Autowired LotStockJpaRepository jpa;
  @Autowired InventoryFixture fixture;

  private long currentVersion(LotStockId id) {
    return jpa.findById(LotStockMapper.toKey(id)).orElseThrow().getVersion();
  }

  @Test
  void version_increments_when_stock_is_updated() {
    var id = fixture.stockLot(fixture.newSku(), fixture.newWarehouse(42.29, -7.81), 25);
    long before = currentVersion(id);

    var stock = repository.find(id).orElseThrow();
    stock.reserve(Quantity.of(1));
    repository.save(stock);

    long after = currentVersion(id);
    assertEquals(before + 1, after);
  }

}
