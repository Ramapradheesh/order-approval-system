
package com.rabtech.order.infrastructure.adapter;

import com.rabtech.order.domain.Order;
import com.rabtech.order.domain.event.DomainEvent;
import com.rabtech.order.domain.event.OrderConfirmed;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InfrastructureAdaptersTest {

    @Test
    void repositoryShouldSaveAndRetrieveOrder() {
        InMemoryOrderRepository repository =
                new InMemoryOrderRepository();

        Order order = Order.create();

        repository.save(order);

        Order retrieved = repository.findById(order.id())
                .orElseThrow();

        assertSame(order, retrieved);
        assertEquals(order.id(), retrieved.id());
    }

    @Test
    void repositoryShouldReturnEmptyForUnknownOrder() {
        InMemoryOrderRepository repository =
                new InMemoryOrderRepository();

        assertTrue(
                repository.findById(UUID.randomUUID()).isEmpty()
        );
    }

    @Test
    void notificationAdapterShouldPublishEvents() {
        InMemoryNotificationAdapter adapter =
                new InMemoryNotificationAdapter();

        DomainEvent event = new OrderConfirmed(
                UUID.randomUUID(),
                Instant.parse("2026-11-23T10:00:00Z")
        );

        adapter.publish(event);

        assertEquals(
                1,
                adapter.publishedEvents().size()
        );

        assertEquals(
                event,
                adapter.publishedEvents().get(0)
        );
    }

    @Test
    void publishedEventsShouldBeImmutable() {
        InMemoryNotificationAdapter adapter =
                new InMemoryNotificationAdapter();

        DomainEvent event = new OrderConfirmed(
                UUID.randomUUID(),
                Instant.parse("2026-11-23T10:00:00Z")
        );

        adapter.publish(event);

        List<DomainEvent> events = adapter.publishedEvents();

        assertThrows(
                UnsupportedOperationException.class,
                () -> events.add(event)
        );
    }

    @Test
    void systemTimeProviderShouldReturnCurrentTime() {
        SystemTimeProvider provider =
                new SystemTimeProvider();

        Instant before = Instant.now();

        Instant actual = provider.now();

        Instant after = Instant.now();

        assertFalse(actual.isBefore(before));
        assertFalse(actual.isAfter(after));
    }
}
