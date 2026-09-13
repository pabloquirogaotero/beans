package com.quirogaotero.beans.ordering.application.port.out;

import com.quirogaotero.beans.ordering.domain.Order;
import com.quirogaotero.beans.ordering.domain.OrderId;

import java.util.Optional;

public interface OrderRepository {

    void save(Order order);

    Optional<Order> findById(OrderId id);

}
