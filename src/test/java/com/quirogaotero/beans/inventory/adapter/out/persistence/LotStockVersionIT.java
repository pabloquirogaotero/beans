package com.quirogaotero.beans.inventory.adapter.out.persistence;

import com.quirogaotero.beans.inventory.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.quirogaotero.beans.TestcontainersConfiguration;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class LotStockVersionIT {

  @Autowired LotStockRepositoryAdapter repository;
  @Autowired LotStockJpaRepository jpa;

  private final LotStockKey key = new LotStockKey(
          UUID.fromString("11111111-1111-1111-1111-111111111111"), "WRH-OUR-01");

  private long currentVersion() {
    return jpa.findById(key).orElseThrow().getVersion();
  }

  @Test
  void version_increments_when_stock_is_updated() {
    long before = currentVersion();

    var id = new LotStockId(new LocationId("WRH-OUR-01"),
            new LotId(UUID.fromString("11111111-1111-1111-1111-111111111111")));
    var stock = repository.find(id).orElseThrow();
    stock.reserve(Quantity.of(1));
    repository.save(stock);

    long after = currentVersion();
    assertEquals(before + 1, after);
  }

}
