
package com.rabtech.order.application.service;

import com.rabtech.order.application.port.NotificationPort;
import com.rabtech.order.application.port.OrderRepository;
import com.rabtech.order.application.port.TimeProvider;
import com.rabtech.order.domain.Order;
import com.rabtech.order.domain.OrderLine;
import com.rabtech.order.domain.event.DomainEvent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class OrderApplicationService {

    private final OrderRepository repository;
    private final NotificationPort notificationPort;
    private final TimeProvider timeProvider;

    public OrderApplicationService(
            OrderRepository repository,
            NotificationPort notificationPort,
            TimeProvider timeProvider) {

        this.repository = Objects.requireNonNull(repository);
        this.notificationPort = Objects.requireNonNull(notificationPort);
        this.timeProvider = Objects.requireNonNull(timeProvider);
    }

    public UUID createOrder() {
        Order order = Order.create();
        repository.save(order);
        return order.id();
    }

    public void addLine(UUID orderId, OrderLine line) {
        Order order = loadOrder(orderId);
        order.addLine(line);
        repository.save(order);
    }

    public void confirmOrder(UUID orderId) {
        Order order = loadOrder(orderId);
        order.confirm(timeProvider.now());
        saveAndPublishEvents(order);
    }

    public void recordPayment(UUID orderId) {
        Order order = loadOrder(orderId);
        order.recordPayment(timeProvider.now());
        saveAndPublishEvents(order);
    }

    public void cancelOrder(UUID orderId) {
        Order order = loadOrder(orderId);
        order.cancel(timeProvider.now());
        saveAndPublishEvents(order);
    }

    public Order getOrder(UUID orderId) {
        return loadOrder(orderId);
    }

    private Order loadOrder(UUID orderId) {
        Objects.requireNonNull(orderId, "Order ID cannot be null");

        return repository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + orderId
                ));
    }

    private void saveAndPublishEvents(Order order) {
        List<DomainEvent> events = order.pullDomainEvents();

        repository.save(order);

        events.forEach(notificationPort::publish);
    }
}
