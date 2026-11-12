package com.quirogaotero.beans.ordering;

import com.jayway.jsonpath.JsonPath;
import com.quirogaotero.beans.TestcontainersConfiguration;
import com.quirogaotero.beans.inventory.InventoryFixture;
import com.quirogaotero.beans.inventory.application.port.out.LotStockRepository;
import com.quirogaotero.beans.inventory.domain.LotStockId;
import com.quirogaotero.beans.inventory.domain.SkuId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Drives the checkout through the real HTTP endpoints, so the whole path a customer hits
 * (controller, transaction boundaries, retry, error mapping) is what gets proven.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestcontainersConfiguration.class, InventoryFixture.class})
class CheckoutHttpIT {

    private static final int STOCK = 40;

    @Value("${local.server.port}") int port;
    @Autowired InventoryFixture fixture;
    @Autowired LotStockRepository lotStocks;
    @Autowired JdbcTemplate jdbc;

    private final HttpClient http = HttpClient.newHttpClient();

    private SkuId sku;
    private LotStockId lotStockId;

    @BeforeEach
    void stockALot() {
        sku = fixture.newSku();
        lotStockId = fixture.stockLot(sku, fixture.newWarehouse(42.29, -7.81), STOCK);
    }

    @Test
    void parallel_checkouts_never_oversell_and_never_fail_with_a_server_error() throws Exception {
        int requests = STOCK + 20;

        var pool      = Executors.newFixedThreadPool(requests);
        var startLine = new CountDownLatch(1);
        var futures   = new ArrayList<Future<Integer>>();
        for (int i = 0; i < requests; i++) {
            futures.add(pool.submit(() -> {
                startLine.await();
                return startCheckout(1).statusCode();
            }));
        }

        startLine.countDown();
        List<Integer> statuses = new ArrayList<>();
        for (Future<Integer> f : futures) statuses.add(f.get(30, TimeUnit.SECONDS));
        pool.shutdown();

        long reserved = statuses.stream().filter(s -> s == 200).count();
        long refused  = statuses.stream().filter(s -> s == 409).count();

        assertEquals(requests, reserved + refused,
                "Every request must end as 200 (reserved) or 409 (refused), got " + statuses);
        assertTrue(reserved > 0, "Some checkouts must succeed");
        assertTrue(reserved <= STOCK, "Must never reserve more than the stock on hand");
        assertEquals(reserved, lotStocks.find(lotStockId).orElseThrow().getReserved().value(),
                "Successful checkouts must match the reserved total in the database");
    }

    @Test
    void pays_a_checkout_once_and_refuses_the_second_payment() throws Exception {
        String checkoutId = JsonPath.read(startCheckout(2).body(), "$.checkoutId");

        HttpResponse<String> paid = pay(checkoutId);
        assertEquals(200, paid.statusCode());
        assertNotNull(JsonPath.<String>read(paid.body(), "$.orderId"));

        assertEquals(409, pay(checkoutId).statusCode());
    }

    @Test
    void refuses_payment_with_409_when_the_hold_has_expired() throws Exception {
        String checkoutId = JsonPath.read(startCheckout(1).body(), "$.checkoutId");
        jdbc.update("UPDATE reservations SET expires_at = now() - interval '1 minute' WHERE checkout_id = ?",
                UUID.fromString(checkoutId));

        HttpResponse<String> response = pay(checkoutId);

        assertEquals(409, response.statusCode());
        assertEquals("Reservation expired", JsonPath.read(response.body(), "$.reason"));
    }

    @Test
    void refuses_a_checkout_with_409_when_stock_is_insufficient() throws Exception {
        HttpResponse<String> response = startCheckout(STOCK + 1);

        assertEquals(409, response.statusCode());
        assertEquals(0, lotStocks.find(lotStockId).orElseThrow().getReserved().value());
    }

    private HttpResponse<String> startCheckout(int quantity) throws Exception {
        return post("/api/checkout", """
                {"latitude": 42.34, "longitude": -7.86,
                 "lines": [{"sku": "%s", "quantity": %d}]}
                """.formatted(sku.value(), quantity));
    }

    private HttpResponse<String> pay(String checkoutId) throws Exception {
        return post("/api/checkout/" + checkoutId + "/pay", """
                {"amountInCents": 1200, "currency": "EUR"}
                """);
    }

    private HttpResponse<String> post(String path, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

}
