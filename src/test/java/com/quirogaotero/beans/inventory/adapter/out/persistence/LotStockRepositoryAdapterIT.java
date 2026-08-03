package com.quirogaotero.beans.inventory.adapter.out.persistence;

import com.quirogaotero.beans.TestcontainersConfiguration;
import com.quirogaotero.beans.inventory.InventoryFixture;
import com.quirogaotero.beans.inventory.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({TestcontainersConfiguration.class, InventoryFixture.class})
class LotStockRepositoryAdapterIT {

  @Autowired LotStockRepositoryAdapter repository;
  @Autowired InventoryFixture fixture;

  private LotStockId lotStockId;

  @BeforeEach
  void stockALot() {
    lotStockId = fixture.stockLot(fixture.newSku(), fixture.newWarehouse(42.29, -7.81), 25);
  }

  @Test
  void finds_a_stored_lot_stock() {
    var found = repository.find(lotStockId);
    assertTrue(found.isPresent());
    assertEquals(25, found.get().getOnHand().value());
  }

  @Test
  @Transactional
  void persists_a_reservation_change_to_available() {
    var stock = repository.find(lotStockId).orElseThrow();
    stock.reserve(Quantity.of(5));
    repository.save(stock);

    var reloaded = repository.find(lotStockId).orElseThrow();
    assertEquals(5, reloaded.getReserved().value());
    assertEquals(20, reloaded.getAvailable().value());
  }

}
