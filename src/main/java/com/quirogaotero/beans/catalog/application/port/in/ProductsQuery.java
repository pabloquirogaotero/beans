package com.quirogaotero.beans.catalog.application.port.in;

import com.quirogaotero.beans.catalog.domain.Product;

import java.util.List;
import java.util.Optional;

/** Catalog is read-only for now (products are managed in the database), so it only has queries. */
public interface ProductsQuery {

  List<Product> findAll();

  Optional<Product> findBySku(String sku);

}
