package com.quirogaotero.beans.inventory;

import com.quirogaotero.beans.TestcontainersConfiguration;
import com.quirogaotero.beans.inventory.application.port.in.*;
import com.quirogaotero.beans.inventory.application.port.out.LotStockRepository;
import com.quirogaotero.beans.inventory.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({TestcontainersConfiguration.class, InventoryFixture.class})
class ReserveConcurrencyIT {

  @Autowired ReserveForCheckoutUseCase reserveForCheckout;
  @Autowired LotStockRepository lotStocks;
  @Autowired InventoryFixture fixture;

  private SkuId sku;
  private LotStockId lotEarlier;
  private LotStockId lotLater;

  @BeforeEach
  void stockTwoLotsOfOneSku() {
    sku = fixture.newSku();
    LocationId warehouse = fixture.newWarehouse(42.29, -7.81);
    lotEarlier = fixture.stockLot(sku, warehouse, 25, LocalDate.now().plusMonths(6));
    lotLater   = fixture.stockLot(sku, warehouse, 40, LocalDate.now().plusMonths(7));
  }

  private ReserveForCheckoutCommand oneUnit() {
    return new ReserveForCheckoutCommand(
            CheckoutId.newCheckoutId(),
            new Coordinates(43.36, -8.41),
            List.of(new RequestedLine(sku, Quantity.of(1))));
  }

  @Test
  void never_oversells_under_parallel_reserves() throws Exception {
    int stockEarlier = lotStocks.find(lotEarlier).orElseThrow().getOnHand().value();
    int stockLater   = lotStocks.find(lotLater).orElseThrow().getOnHand().value();
    int totalWarehouseStock = stockEarlier + stockLater;

    int excess  = 20;
    int threads = totalWarehouseStock + excess;

    var pool      = Executors.newFixedThreadPool(threads);
    var startLine = new CountDownLatch(1);
    var reserved  = new AtomicInteger(0);
    var soldOut   = new AtomicInteger(0);

    var futures = new java.util.ArrayList<Future<?>>();
    for (int i = 0; i < threads; i++) {
      futures.add(pool.submit(() -> {
        startLine.await();
        ReservationResult result = reserveForCheckout.reserveForCheckout(oneUnit());
        (result.reserved() ? reserved : soldOut).incrementAndGet();
        return null;
      }));
    }

    startLine.countDown();
    for (Future<?> f : futures) f.get(30, TimeUnit.SECONDS);
    pool.shutdown();

    var finalEarlier = lotStocks.find(lotEarlier).orElseThrow();
    var finalLater   = lotStocks.find(lotLater).orElseThrow();

    assertTrue(finalEarlier.getReserved().value() <= finalEarlier.getOnHand().value(),
            "Earlier lot must never be oversold");
    assertTrue(finalLater.getReserved().value() <= finalLater.getOnHand().value(),
            "Later lot must never be oversold");
    assertTrue(reserved.get() <= totalWarehouseStock,
            "Must never reserve more than the total available warehouse stock");

    int actuallyReserved = finalEarlier.getReserved().value() + finalLater.getReserved().value();
    assertEquals(reserved.get(), actuallyReserved,
            "Successful-reservation count must match the DB reserved totals");

    assertEquals(threads, reserved.get() + soldOut.get(), "All requests must be accounted for");
    assertTrue(reserved.get() > 0, "Some reservations must succeed");
  }
}