package com.quirogaotero.beans.catalog;

import com.jayway.jsonpath.JsonPath;
import com.quirogaotero.beans.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class ProductsHttpIT {

  @Value("${local.server.port}") int port;

  @Test
  void lists_the_products_with_their_prices() throws Exception {
    HttpResponse<String> response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/products")).GET().build(),
            HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode());
    assertEquals(List.of(1250), JsonPath.read(response.body(), "$[?(@.sku == 'COF-COLHU-WBN-250')].priceInCents"));
    assertEquals(List.of("EUR"), JsonPath.read(response.body(), "$[?(@.sku == 'COF-COLHU-WBN-250')].currency"));
  }

}
