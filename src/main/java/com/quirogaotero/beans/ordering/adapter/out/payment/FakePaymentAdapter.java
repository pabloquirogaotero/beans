package com.quirogaotero.beans.ordering.adapter.out.payment;

import com.quirogaotero.beans.inventory.domain.CheckoutId;
import com.quirogaotero.beans.ordering.application.port.out.PaymentPort;
import com.quirogaotero.beans.ordering.application.port.out.PaymentResult;
import com.quirogaotero.beans.ordering.domain.Money;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FakePaymentAdapter implements PaymentPort {

    @Override
    public PaymentResult charge(CheckoutId checkoutId, Money amount) {
        // Simulated gateway: always approves.
        return PaymentResult.approved("FAKE-" + UUID.randomUUID());
    }

}
