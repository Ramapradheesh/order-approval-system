
package com.rabtech.order.domain.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record OrderCancelled(
        UUID orderId,
        Instant occurredAt
) implements DomainEvent {

    public OrderCancelled {
        Objects.requireNonNull(
                orderId,
                "Order ID cannot be null"
        );

        Objects.requireNonNull(
                occurredAt,
                "Event time cannot be null"
        );
    }
}
