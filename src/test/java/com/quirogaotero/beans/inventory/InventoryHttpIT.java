package com.quirogaotero.beans.inventory;

import com.jayway.jsonpath.JsonPath;
import com.quirogaotero.beans.TestcontainersConfiguration;
import com.quirogaotero.beans.inventory.domain.LotCode;
import com.quirogaotero.beans.inventory.domain.LotStockId;
import com.quirogaotero.beans.inventory.domain.SkuId;
import com.quirogaotero.beans.ordering.application.CheckoutOutcome;
import com.quirogaotero.beans.ordering.application.PayCheckoutResult;
import com.quirogaotero.beans.ordering.application.PayCheckoutService;
import com.quirogaotero.beans.ordering.application.StartCheckoutCommand;
import com.quirogaotero.beans.ordering.application.StartCheckoutService;
import com.quirogaotero.beans.ordering.domain.Money;
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
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestcontainersConfiguration.class, InventoryFixture.class})
class InventoryHttpIT {

    @Value("${local.server.port}") int port;
    @Autowired InventoryFixture fixture;
    @Autowired StartCheckoutService startCheckout;
    @Autowired PayCheckoutService payCheckout;
    @Autowired JdbcTemplate jdbc;

    private final HttpClient http = HttpClient.newHttpClient();

    private SkuId sku;
    private LotStockId earlierLot;
    private LotStockId laterLot;

    @BeforeEach
    void stockTwoLots() {
        sku = fixture.newSku();
        var warehouse = fixture.newWarehouse(42.29, -7.81);
        earlierLot = fixture.stockLot(sku, warehouse, 10, LocalDate.now().plusMonths(6));
        laterLot   = fixture.stockLot(sku, warehouse, 10, LocalDate.now().plusMonths(7));
    }

    @Test
    void shows_stock_per_lot_including_what_is_held_by_checkouts() throws Exception {
        startCheckoutFor(4);

        HttpResponse<String> response = get("/api/stock/" + sku.value());

        assertEquals(200, response.statusCode());
        String earlier = "$.levels[?(@.lotCode == '" + fixture.lotCodeOf(earlierLot).value() + "')]";
        assertEquals(List.of(10), JsonPath.read(response.body(), earlier + ".onHand"));
        assertEquals(List.of(4), JsonPath.read(response.body(), earlier + ".reserved"));
        assertEquals(List.of(6), JsonPath.read(response.body(), earlier + ".available"));
        String later = "$.levels[?(@.lotCode == '" + fixture.lotCodeOf(laterLot).value() + "')]";
        assertEquals(List.of(10), JsonPath.read(response.body(), later + ".available"));
    }

    @Test
    void traces_a_lot_to_the_orders_that_received_it() throws Exception {
        UUID firstOrder = payForUnits(3);
        UUID secondOrder = payForUnits(2);
        awaitFulfilled(firstOrder);
        awaitFulfilled(secondOrder);

        LotCode code = fixture.lotCodeOf(earlierLot);
        HttpResponse<String> response = get("/api/lots/" + code.value() + "/trace");

        assertEquals(200, response.statusCode());
        assertEquals(sku.value(), JsonPath.read(response.body(), "$.sku"));
        // The two orders may be picked in either order (different partitions), so compare as a set.
        List<String> orderIds = JsonPath.read(response.body(), "$.shipments[*].orderId");
        List<Integer> quantities = JsonPath.read(response.body(), "$.shipments[*].quantity");
        Map<String, Integer> shipped = new HashMap<>();
        for (int i = 0; i < orderIds.size(); i++) shipped.put(orderIds.get(i), quantities.get(i));
        assertEquals(Map.of(firstOrder.toString(), 3, secondOrder.toString(), 2), shipped);

        HttpResponse<String> untouched = get("/api/lots/" + fixture.lotCodeOf(laterLot).value() + "/trace");
        assertEquals(List.of(), JsonPath.read(untouched.body(), "$.shipments"),
                "FEFO took every unit from the earlier lot, so the later one reached nobody");
    }

    @Test
    void answers_404_for_a_lot_that_does_not_exist() throws Exception {
        assertEquals(404, get("/api/lots/NOSUCHLOT/trace").statusCode());
    }

    private CheckoutOutcome startCheckoutFor(int quantity) {
        CheckoutOutcome outcome = startCheckout.startCheckout(new StartCheckoutCommand(42.34, -7.86,
                List.of(new StartCheckoutCommand.Line(sku.value(), quantity))));
        assertTrue(outcome.result().reserved());
        return outcome;
    }

    private UUID payForUnits(int quantity) {
        PayCheckoutResult paid = payCheckout.pay(startCheckoutFor(quantity).checkoutId(), Money.euros(1200));
        assertTrue(paid.paid());
        return paid.orderId().value();
    }

    private void awaitFulfilled(UUID orderId) {
        await().atMost(Duration.ofSeconds(30)).until(() -> "FULFILLED".equals(
                jdbc.queryForObject("SELECT status FROM orders WHERE id = ?", String.class, orderId)));
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

}
