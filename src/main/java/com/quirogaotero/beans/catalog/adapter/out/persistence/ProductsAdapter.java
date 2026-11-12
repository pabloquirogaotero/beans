package com.quirogaotero.beans.catalog.adapter.out.persistence;

import com.quirogaotero.beans.catalog.application.port.in.ProductsQuery;
import com.quirogaotero.beans.catalog.domain.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductsAdapter implements ProductsQuery {

  private static final RowMapper<Product> PRODUCT = (rs, i) -> new Product(
          rs.getString("sku"), rs.getString("name"), rs.getLong("price_in_cents"), rs.getString("currency"));

  private final JdbcTemplate jdbc;

  public ProductsAdapter(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public List<Product> findAll() {
    return jdbc.query("SELECT sku, name, price_in_cents, currency FROM products ORDER BY sku", PRODUCT);
  }

  @Override
  public Optional<Product> findBySku(String sku) {
    return jdbc.query("SELECT sku, name, price_in_cents, currency FROM products WHERE sku = ?", PRODUCT, sku)
            .stream().findFirst();
  }

}
