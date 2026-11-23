
package com.rabtech.order.infrastructure;

import com.rabtech.order.application.port.TimeProvider;
import com.rabtech.order.application.service.OrderApplicationService;
import com.rabtech.order.domain.Order;
import com.rabtech.order.domain.OrderLine;
import com.rabtech.order.domain.OrderStatus;
import com.rabtech.order.domain.event.DomainEvent;
import com.rabtech.order.domain.event.OrderConfirmed;
import com.rabtech.order.domain.event.OrderCancelled;
import com.rabtech.order.domain.event.PaymentRecorded;
import com.rabtech.order.infrastructure.adapter.InMemoryNotificationAdapter;
import com.rabtech.order.infrastructure.adapter.InMemoryOrderRepository;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderWorkflowIntegrationTest {

    private static final Instant NOW =
            Instant.parse("2026-11-23T10:00:00Z");

    @Test
    void shouldCompleteOrderApprovalAndPaymentWorkflow() {

        InMemoryOrderRepository repository =
                new InMemoryOrderRepository();

        InMemoryNotificationAdapter notifications =
                new InMemoryNotificationAdapter();

        TimeProvider timeProvider = () -> NOW;

        OrderApplicationService service =
                new OrderApplicationService(
                        repository,
                        notifications,
                        timeProvider
                );

        // Create an order
        UUID orderId = service.createOrder();

        // Add products
        service.addLine(
                orderId,
                new OrderLine(
                        "PRODUCT-101",
                        2,
                        new BigDecimal("100.00")
                )
        );

        // Confirm order
        service.confirmOrder(orderId);

        // Record payment
        service.recordPayment(orderId);

        // Verify final order
        Order order = service.getOrder(orderId);

        assertEquals(OrderStatus.PAID, order.status());

        assertEquals(
                new BigDecimal("200.00"),
                order.total()
        );

        // Verify published events
        List<DomainEvent> events =
                notifications.publishedEvents();

        assertEquals(2, events.size());

        assertInstanceOf(
                OrderConfirmed.class,
                events.get(0)
        );

        assertInstanceOf(
                PaymentRecorded.class,
                events.get(1)
        );
    }

    @Test
    void cancelledOrderShouldNotAcceptPayment() {

        InMemoryOrderRepository repository =
                new InMemoryOrderRepository();

        InMemoryNotificationAdapter notifications =
                new InMemoryNotificationAdapter();

        OrderApplicationService service =
                new OrderApplicationService(
                        repository,
                        notifications,
                        () -> NOW
                );

        UUID orderId = service.createOrder();

        service.addLine(
                orderId,
                new OrderLine(
                        "PRODUCT-202",
                        1,
                        new BigDecimal("500.00")
                )
        );

        service.confirmOrder(orderId);
        service.cancelOrder(orderId);

        assertEquals(
                OrderStatus.CANCELLED,
                service.getOrder(orderId).status()
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.recordPayment(orderId)
        );

        List<DomainEvent> events =
                notifications.publishedEvents();

        assertEquals(2, events.size());

        assertInstanceOf(
                OrderCancelled.class,
                events.get(1)
        );
    }
}
