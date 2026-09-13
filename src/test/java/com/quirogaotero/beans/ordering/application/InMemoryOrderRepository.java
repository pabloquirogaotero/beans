package com.quirogaotero.beans.ordering.application;

import com.quirogaotero.beans.ordering.application.port.out.OrderRepository;
import com.quirogaotero.beans.ordering.domain.Order;
import com.quirogaotero.beans.ordering.domain.OrderId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class InMemoryOrderRepository implements OrderRepository {

    private final Map<OrderId, Order> store = new HashMap<>();

    public void save(Order order) { store.put(order.getId(), order); }

    public Optional<Order> findById(OrderId id) { return Optional.ofNullable(store.get(id)); }

}