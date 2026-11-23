
package com.rabtech.order.domain.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {

    UUID orderId();

    Instant occurredAt();
}
