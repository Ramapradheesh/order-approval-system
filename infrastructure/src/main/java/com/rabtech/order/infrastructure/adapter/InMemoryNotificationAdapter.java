
package com.rabtech.order.infrastructure.adapter;

import com.rabtech.order.application.port.NotificationPort;
import com.rabtech.order.domain.event.DomainEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryNotificationAdapter implements NotificationPort {

    private final List<DomainEvent> publishedEvents =
            new CopyOnWriteArrayList<>();

    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "Domain event cannot be null"
            );
        }

        publishedEvents.add(event);
    }

    public List<DomainEvent> publishedEvents() {
        return List.copyOf(publishedEvents);
    }
}
