
package com.rabtech.order.application.service;

import com.rabtech.order.application.port.NotificationPort;
import com.rabtech.order.application.port.OrderRepository;
import com.rabtech.order.application.port.TimeProvider;
import com.rabtech.order.domain.Order;
import com.rabtech.order.domain.OrderLine;
import com.rabtech.order.domain.OrderStatus;
import com.rabtech.order.domain.event.DomainEvent;
import com.rabtech.order.domain.event.OrderConfirmed;
import com.rabtech.order.domain.event.PaymentRecorded;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class OrderApplicationServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-11-23T10:00:00Z");

    private static class FakeRepository
            implements OrderRepository {

        private final Map<UUID, Order> orders = new HashMap<>();

        @Override
        public void save(Order order) {
            orders.put(order.id(), order);
        }

        @Override
        public Optional<Order> findById(UUID id) {
            return Optional.ofNullable(orders.get(id));
        }
    }

    private static class FakeNotification
            implements NotificationPort {

        private final List<DomainEvent> events =
                new ArrayList<>();

        @Override
        public void publish(DomainEvent event) {
            events.add(event);
        }
    }

    @Test
    void shouldCreateAndPersistOrder() {
        FakeRepository repository = new FakeRepository();
        FakeNotification notification = new FakeNotification();

        OrderApplicationService service =
                new OrderApplicationService(
                        repository, notification, () -> NOW
                );

        UUID id = service.createOrder();

        assertNotNull(id);
        assertTrue(repository.findById(id).isPresent());
        assertEquals(
                OrderStatus.DRAFT,
                service.getOrder(id).status()
        );
    }

    @Test
    void shouldAddOrderLine() {
        FakeRepository repository = new FakeRepository();

        OrderApplicationService service =
                new OrderApplicationService(
                        repository,
                        event -> {},
                        () -> NOW
                );

        UUID id = service.createOrder();

        service.addLine(
                id,
                new OrderLine(
                        "PRODUCT-1", 2,
                        new BigDecimal("100.00")
                )
        );

        assertEquals(
                new BigDecimal("200.00"),
                service.getOrder(id).total()
        );
    }

    @Test
    void shouldConfirmOrderAndPublishEvent() {
        FakeRepository repository = new FakeRepository();
        FakeNotification notification = new FakeNotification();

        OrderApplicationService service =
                new OrderApplicationService(
                        repository, notification, () -> NOW
                );

        UUID id = service.createOrder();

        service.addLine(
                id,
                new OrderLine(
                        "PRODUCT-1", 1,
                        new BigDecimal("250.00")
                )
        );

        service.confirmOrder(id);

        assertEquals(
                OrderStatus.CONFIRMED,
                service.getOrder(id).status()
        );

        assertEquals(1, notification.events.size());

        assertInstanceOf(
                OrderConfirmed.class,
                notification.events.get(0)
        );
    }

    @Test
    void shouldRecordPaymentAndPublishEvent() {
        FakeRepository repository = new FakeRepository();
        FakeNotification notification = new FakeNotification();

        OrderApplicationService service =
                new OrderApplicationService(
                        repository, notification, () -> NOW
                );

        UUID id = service.createOrder();

        service.addLine(
                id,
                new OrderLine(
                        "PRODUCT-1", 1,
                        new BigDecimal("300.00")
                )
        );

        service.confirmOrder(id);
        service.recordPayment(id);

        assertEquals(
                OrderStatus.PAID,
                service.getOrder(id).status()
        );

        assertEquals(2, notification.events.size());

        assertInstanceOf(
                PaymentRecorded.class,
                notification.events.get(1)
        );
    }

    @Test
    void shouldRejectConfirmationOfEmptyOrder() {
        FakeRepository repository = new FakeRepository();

        OrderApplicationService service =
                new OrderApplicationService(
                        repository,
                        event -> {},
                        () -> NOW
                );

        UUID id = service.createOrder();

        assertThrows(
                IllegalStateException.class,
                () -> service.confirmOrder(id)
        );

        assertEquals(
                OrderStatus.DRAFT,
                service.getOrder(id).status()
        );
    }

    @Test
    void shouldRejectUnknownOrder() {
        FakeRepository repository = new FakeRepository();

        OrderApplicationService service =
                new OrderApplicationService(
                        repository,
                        event -> {},
                        () -> NOW
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getOrder(UUID.randomUUID())
        );
    }
}
