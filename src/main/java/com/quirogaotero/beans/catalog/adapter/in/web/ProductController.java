package com.quirogaotero.beans.catalog.adapter.in.web;

import com.quirogaotero.beans.catalog.application.port.in.ProductsQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

  private final ProductsQuery products;

  public ProductController(ProductsQuery products) {
    this.products = products;
  }

  @GetMapping
  public List<ProductResponse> products() {
    return products.findAll().stream()
            .map(p -> new ProductResponse(p.sku(), p.name(), p.priceInCents(), p.currency()))
            .toList();
  }

  public record ProductResponse(String sku, String name, long priceInCents, String currency) { }

}
