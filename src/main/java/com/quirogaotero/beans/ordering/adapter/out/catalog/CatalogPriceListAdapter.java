package com.quirogaotero.beans.ordering.adapter.out.catalog;

import com.quirogaotero.beans.catalog.application.port.in.ProductsQuery;
import com.quirogaotero.beans.ordering.application.port.out.PriceList;
import com.quirogaotero.beans.ordering.domain.Money;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Ordering's window into Catalog. The translation to Ordering's own Money happens here, so no
 * Catalog type reaches Ordering's application or domain code.
 */
@Component
public class CatalogPriceListAdapter implements PriceList {

    private final ProductsQuery products;

    public CatalogPriceListAdapter(ProductsQuery products) { this.products = products; }

    @Override
    public Optional<Money> unitPriceOf(String sku) {
        return products.findBySku(sku).map(p -> new Money(p.priceInCents(), p.currency()));
    }

}
