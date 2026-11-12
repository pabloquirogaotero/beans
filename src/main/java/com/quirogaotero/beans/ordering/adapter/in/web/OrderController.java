package com.quirogaotero.beans.ordering.adapter.in.web;

import com.quirogaotero.beans.ordering.application.GetOrderService;
import com.quirogaotero.beans.ordering.domain.Order;
import com.quirogaotero.beans.ordering.domain.OrderId;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final GetOrderService getOrder;

    public OrderController(GetOrderService getOrder) { this.getOrder = getOrder; }

    @GetMapping("/{orderId}")
    public OrderResponse order(@PathVariable UUID orderId) {
        Order order = getOrder.getOrder(new OrderId(orderId));
        return new OrderResponse(order.getId().value(), order.getCheckoutId().value(), order.getStatus().name(),
                order.getLines().stream().map(l -> new OrderResponse.Line(l.sku(), l.quantity())).toList(),
                order.getPlacedAt());
    }

}
