package com.quirogaotero.beans;

import com.quirogaotero.beans.inventory.InventoryFixture;
import com.quirogaotero.beans.inventory.application.port.out.LotStockRepository;
import com.quirogaotero.beans.inventory.domain.LotStock;
import com.quirogaotero.beans.inventory.domain.LotStockId;
import com.quirogaotero.beans.inventory.domain.SkuId;
import com.quirogaotero.beans.messaging.KafkaTestConsumer;
import com.quirogaotero.beans.ordering.application.CheckoutOutcome;
import com.quirogaotero.beans.ordering.application.PayCheckoutResult;
import com.quirogaotero.beans.ordering.application.PayCheckoutService;
import com.quirogaotero.beans.ordering.application.StartCheckoutCommand;
import com.quirogaotero.beans.ordering.application.StartCheckoutService;
import com.quirogaotero.beans.ordering.domain.Money;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.kafka.KafkaContainer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The asynchronous tail of an order, across both contexts and a real Kafka broker:
 * pay (Ordering) -> OrderPlaced -> pick stock (Inventory) -> StockPicked -> fulfilled (Ordering).
 */
@SpringBootTest
@Import({TestcontainersConfiguration.class, InventoryFixture.class})
class FulfilmentFlowIT {

    private static final int STOCK = 10;

    @Autowired StartCheckoutService startCheckout;
    @Autowired PayCheckoutService payCheckout;
    @Autowired LotStockRepository lotStocks;
    @Autowired InventoryFixture fixture;
    @Autowired JdbcTemplate jdbc;
    @Autowired KafkaTemplate<String, String> kafkaTemplate;
    @Autowired KafkaContainer kafka;
    @Value("${beans.messaging.topics.order-events}") String orderEventsTopic;

    private SkuId sku;
    private LotStockId lotStockId;

    @BeforeEach
    void stockALot() {
        sku = fixture.newSku();
        lotStockId = fixture.stockLot(sku, fixture.newWarehouse(42.29, -7.81), STOCK);
    }

    @Test
    void a_paid_order_has_its_stock_picked_and_ends_fulfilled() {
        UUID orderId = payForUnits(3);

        awaitFulfilled(orderId);

        LotStock stock = lotStocks.find(lotStockId).orElseThrow();
        assertEquals(STOCK - 3, stock.getOnHand().value(), "Picked units leave the stock on hand");
        assertEquals(0, stock.getReserved().value(), "Picked units are no longer reserved");
        assertEquals("PICKED", jdbc.queryForObject(
                "SELECT status FROM reservations WHERE lot_id = ?", String.class, lotStockId.lotId().value()));
    }

    @Test
    void a_redelivered_order_placed_event_is_skipped_without_picking_twice() {
        UUID orderId = payForUnits(3);
        awaitFulfilled(orderId);

        // Kafka delivers at least once: replay the exact message the relay already published.
        Map<String, Object> original = jdbc.queryForMap(
                "SELECT id, payload::text AS payload FROM outbox_events WHERE type = 'OrderPlaced' AND message_key = ?",
                orderId.toString());
        kafkaTemplate.send(orderPlaced(null, orderId.toString(),
                (String) original.get("payload"), original.get("id").toString()));

        // A duplicate that was not recognised would fail (nothing left to pick) and be dead-lettered.
        assertTrue(readDeadLetters(r -> orderId.toString().equals(r.key()), 1, Duration.ofSeconds(6)).isEmpty(),
                "The duplicate must be skipped, not retried and dead-lettered");
        assertEquals(STOCK - 3, lotStocks.find(lotStockId).orElseThrow().getOnHand().value());
        assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM outbox_events WHERE type = 'StockPicked' AND message_key = ?",
                Integer.class, orderId.toString()), "Stock must be announced as picked exactly once");
    }

    @Test
    void a_message_that_cannot_be_handled_is_dead_lettered_and_does_not_block_later_orders() {
        // One unreadable message on every partition, so whichever partition the order lands on has one ahead of it.
        String poison = "not-json-" + UUID.randomUUID();
        for (int partition = 0; partition < 3; partition++)
            kafkaTemplate.send(orderPlaced(partition, "poison", poison, UUID.randomUUID().toString()));

        UUID orderId = payForUnits(1);

        awaitFulfilled(orderId);
        assertEquals(3, readDeadLetters(r -> poison.equals(r.value()), 3, Duration.ofSeconds(20)).size(),
                "Every unreadable message must end on the dead-letter topic");
    }

    private UUID payForUnits(int quantity) {
        CheckoutOutcome checkout = startCheckout.startCheckout(new StartCheckoutCommand(42.34, -7.86,
                List.of(new StartCheckoutCommand.Line(sku.value(), quantity))));
        assertTrue(checkout.result().reserved());

        PayCheckoutResult paid = payCheckout.pay(checkout.checkoutId(), Money.euros(1200));
        assertTrue(paid.paid());
        return paid.orderId().value();
    }

    private void awaitFulfilled(UUID orderId) {
        await().atMost(Duration.ofSeconds(30)).until(() -> "FULFILLED".equals(
                jdbc.queryForObject("SELECT status FROM orders WHERE id = ?", String.class, orderId)));
    }

    private ProducerRecord<String, String> orderPlaced(Integer partition, String key, String value, String eventId) {
        var record = new ProducerRecord<>(orderEventsTopic, partition, key, value);
        record.headers().add("event-id", eventId.getBytes(StandardCharsets.UTF_8));
        record.headers().add("event-type", "OrderPlaced".getBytes(StandardCharsets.UTF_8));
        return record;
    }

    private List<ConsumerRecord<String, String>> readDeadLetters(
            Predicate<ConsumerRecord<String, String>> matching, int expected, Duration atMost) {
        return KafkaTestConsumer.read(kafka.getBootstrapServers(), orderEventsTopic + ".DLT", matching, expected, atMost);
    }

}
