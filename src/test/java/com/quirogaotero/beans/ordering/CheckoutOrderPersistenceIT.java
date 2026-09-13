package com.quirogaotero.beans.ordering;

import com.quirogaotero.beans.TestcontainersConfiguration;
import com.quirogaotero.beans.inventory.domain.CheckoutId;
import com.quirogaotero.beans.ordering.adapter.out.persistence.CheckoutRepositoryAdapter;
import com.quirogaotero.beans.ordering.adapter.out.persistence.OrderRepositoryAdapter;
import com.quirogaotero.beans.ordering.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CheckoutOrderPersistenceIT {

    @Autowired
    CheckoutRepositoryAdapter checkouts;
    @Autowired
    OrderRepositoryAdapter orders;

    @Test
    void checkout_round_trips_with_its_lines() {
        var checkout = Checkout.start(List.of(new OrderLine("COF-COLHU-WBN-250", 2)), Instant.now());
        checkouts.save(checkout);

        var loaded = checkouts.findById(checkout.getId()).orElseThrow();
        assertEquals(CheckoutStatus.STARTED, loaded.getStatus());
        assertEquals(1, loaded.getLines().size());
        assertEquals("COF-COLHU-WBN-250", loaded.getLines().getFirst().sku());
    }

    @Test
    void order_round_trips_with_its_lines() {
        var order = Order.place(CheckoutId.newCheckoutId(),
                List.of(new OrderLine("COF-ETHYI-WBN-250", 3)), Instant.now());
        orders.save(order);

        var loaded = orders.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, loaded.getStatus());
        assertEquals(3, loaded.getLines().getFirst().quantity());
    }

}