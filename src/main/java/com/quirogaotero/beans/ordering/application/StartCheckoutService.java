package com.quirogaotero.beans.ordering.application;

import com.quirogaotero.beans.inventory.application.port.in.*;
import com.quirogaotero.beans.inventory.domain.*;
import com.quirogaotero.beans.ordering.application.port.out.CheckoutRepository;
import com.quirogaotero.beans.ordering.application.port.out.PriceList;
import com.quirogaotero.beans.ordering.domain.Checkout;
import com.quirogaotero.beans.ordering.domain.Money;
import com.quirogaotero.beans.ordering.domain.OrderLine;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

@Service
public class StartCheckoutService {

    private final ReserveForCheckoutUseCase reserveForCheckout;
    private final CheckoutRepository checkouts;
    private final PriceList priceList;
    private final Clock clock;

    public StartCheckoutService(ReserveForCheckoutUseCase reserveForCheckout,
                                CheckoutRepository checkouts, PriceList priceList, Clock clock) {
        this.reserveForCheckout = reserveForCheckout;
        this.checkouts = checkouts;
        this.priceList = priceList;
        this.clock = clock;
    }

    // Deliberately NOT @Transactional. The reserve must commit in its own transaction so that
    // the optimistic-lock failure surfaces inside the retry wrapper; an outer transaction here
    // would defer the version check to its own commit, after the retries have already returned.
    public CheckoutOutcome startCheckout(StartCheckoutCommand command) {
        // 1. Ordering owns what the customer chose — price it and create the Checkout (not persisted yet).
        //    Pricing comes first: an unknown product is refused before any stock is touched.
        List<OrderLine> lines = command.lines().stream()
                .map(l -> new OrderLine(l.sku(), l.quantity()))
                .toList();
        Checkout checkout = Checkout.start(lines, totalOf(lines), clock.instant());

        // 2. Reserve stock in Inventory, using this checkout's id.
        List<RequestedLine> reserveLines = command.lines().stream()
                .map(l -> new RequestedLine(new SkuId(l.sku()), Quantity.of(l.quantity())))
                .toList();
        Coordinates destination = new Coordinates(command.latitude(), command.longitude());

        ReservationResult result = reserveForCheckout.reserveForCheckout(
                new ReserveForCheckoutCommand(checkout.getId(), destination, reserveLines));

        // 3. Persist the Checkout only once it holds stock. If this save fails, the holds are
        //    orphaned but harmless: they expire and the sweep returns their stock.
        if (result.reserved())
            checkouts.save(checkout);

        return new CheckoutOutcome(checkout.getId(), checkout.getTotal(), result);
    }

    private Money totalOf(List<OrderLine> lines) {
        return lines.stream()
                .map(l -> priceList.unitPriceOf(l.sku())
                        .orElseThrow(() -> new UnknownProductException(l.sku()))
                        .times(l.quantity()))
                .reduce(Money::plus)
                .orElseThrow();
    }
}
