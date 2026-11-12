package com.quirogaotero.beans.ordering.application.port.out;

import com.quirogaotero.beans.ordering.domain.Money;

import java.util.Optional;

/** Current unit prices. Empty when the SKU is not something we sell. */
public interface PriceList {

    Optional<Money> unitPriceOf(String sku);

}
