
package com.rabtech.order.infrastructure.adapter;

import com.rabtech.order.application.port.OrderRepository;
import com.rabtech.order.domain.Order;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOrderRepository implements OrderRepository {

    private final Map<UUID, Order> orders = new ConcurrentHashMap<>();

    @Override
    public void save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException(
                    "Order cannot be null"
            );
        }

        orders.put(order.id(), order);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException(
                    "Order ID cannot be null"
            );
        }

        return Optional.ofNullable(orders.get(orderId));
    }
}
