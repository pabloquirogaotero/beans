package com.quirogaotero.beans.ordering.adapter.out.persistence;

import com.quirogaotero.beans.ordering.application.port.out.OrderRepository;
import com.quirogaotero.beans.ordering.domain.Order;
import com.quirogaotero.beans.ordering.domain.OrderId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpa;

    public OrderRepositoryAdapter(OrderJpaRepository jpa) { this.jpa = jpa; }

    @Override public void save(Order order) { jpa.save(OrderMapper.toEntity(order)); }

    @Override public Optional<Order> findById(OrderId id) {
        return jpa.findById(id.value()).map(OrderMapper::toDomain);
    }

}
