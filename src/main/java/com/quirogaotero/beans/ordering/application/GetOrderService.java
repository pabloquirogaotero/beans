package com.quirogaotero.beans.ordering.application;

import com.quirogaotero.beans.ordering.application.port.out.OrderRepository;
import com.quirogaotero.beans.ordering.domain.Order;
import com.quirogaotero.beans.ordering.domain.OrderId;
import org.springframework.stereotype.Service;

@Service
public class GetOrderService {

    private final OrderRepository orders;

    public GetOrderService(OrderRepository orders) { this.orders = orders; }

    public Order getOrder(OrderId id) {
        return orders.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

}
