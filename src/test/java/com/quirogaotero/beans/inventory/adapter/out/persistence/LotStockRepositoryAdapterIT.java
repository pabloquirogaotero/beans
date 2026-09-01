package com.quirogaotero.beans.inventory.adapter.out.persistence;

import com.quirogaotero.beans.TestcontainersConfiguration;
import com.quirogaotero.beans.inventory.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class LotStockRepositoryAdapterIT {

  @Autowired LotStockRepositoryAdapter repository;

  private LotStockId seededId() {
    return new LotStockId(new LocationId("WRH-OUR-01"),
            new LotId(java.util.UUID.fromString("11111111-1111-1111-1111-111111111111")));
  }

  @Test
  void finds_a_seeded_lot_stock() {
    var found = repository.find(seededId());
    assertTrue(found.isPresent());
    assertEquals(25, found.get().getOnHand().value());
  }

  @Test
  @Transactional
  void persists_a_reservation_change_to_available() {
    var stock = repository.find(seededId()).orElseThrow();
    stock.reserve(Quantity.of(5));
    repository.save(stock);

    var reloaded = repository.find(seededId()).orElseThrow();
    assertEquals(5, reloaded.getReserved().value());
    assertEquals(20, reloaded.getAvailable().value());
  }

}
