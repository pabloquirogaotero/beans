package com.quirogaotero.beans.ordering.application.port.out;

import com.quirogaotero.beans.inventory.domain.CheckoutId;
import com.quirogaotero.beans.ordering.domain.Money;

public interface PaymentPort {

    PaymentResult charge(CheckoutId checkoutId, Money amount);

}