
package com.rabtech.order.domain;

import com.rabtech.order.domain.event.DomainEvent;
import com.rabtech.order.domain.event.OrderConfirmed;
import com.rabtech.order.domain.event.OrderCancelled;
import com.rabtech.order.domain.event.PaymentRecorded;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Order {

    private final UUID id;
    private final List<OrderLine> lines;
    private final List<DomainEvent> domainEvents;

    private OrderStatus status;

    public Order(UUID id) {
        this.id = Objects.requireNonNull(
                id, "Order ID cannot be null"
        );

        this.lines = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
        this.status = OrderStatus.DRAFT;
    }

    public static Order create() {
        return new Order(UUID.randomUUID());
    }

    public void addLine(OrderLine line) {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException(
                    "Lines can only be added to draft orders"
            );
        }

        lines.add(Objects.requireNonNull(
                line, "Order line cannot be null"
        ));
    }

    public void confirm(Instant occurredAt) {
        Objects.requireNonNull(occurredAt, "Event time cannot be null");

        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only draft orders can be confirmed"
            );
        }

        if (lines.isEmpty()) {
            throw new IllegalStateException(
                    "An order must contain at least one line before confirmation"
            );
        }

        status = OrderStatus.CONFIRMED;

        domainEvents.add(
                new OrderConfirmed(id, occurredAt)
        );
    }

    public void recordPayment(Instant occurredAt) {
        Objects.requireNonNull(occurredAt, "Event time cannot be null");

        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "A cancelled order cannot be paid"
            );
        }

        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Only confirmed orders can be paid"
            );
        }

        status = OrderStatus.PAID;

        domainEvents.add(
                new PaymentRecorded(id, occurredAt)
        );
    }

    public void cancel(Instant occurredAt) {
        Objects.requireNonNull(occurredAt, "Event time cannot be null");

        if (status == OrderStatus.PAID ||
                status == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "This order cannot be cancelled"
            );
        }

        status = OrderStatus.CANCELLED;

        domainEvents.add(
                new OrderCancelled(id, occurredAt)
        );
    }

    public BigDecimal total() {
        return lines.stream()
                .map(OrderLine::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public UUID id() {
        return id;
    }

    public OrderStatus status() {
        return status;
    }

    public List<OrderLine> lines() {
        return List.copyOf(lines);
    }
}
