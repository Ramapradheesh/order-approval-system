
package com.rabtech.order.domain;

import com.rabtech.order.domain.event.DomainEvent;
import com.rabtech.order.domain.event.OrderConfirmed;
import com.rabtech.order.domain.event.PaymentRecorded;
import com.rabtech.order.domain.event.OrderCancelled;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private static final Instant NOW =
            Instant.parse("2026-11-23T10:00:00Z");

    @Test
    void newOrderShouldStartAsDraft() {
        Order order = Order.create();

        assertEquals(OrderStatus.DRAFT, order.status());
        assertTrue(order.lines().isEmpty());
        assertEquals(BigDecimal.ZERO, order.total());
    }

    @Test
    void shouldRejectConfirmationWithoutLines() {
        Order order = Order.create();

        assertThrows(
                IllegalStateException.class,
                () -> order.confirm(NOW)
        );
    }

    @Test
    void shouldCalculateOrderTotal() {
        Order order = Order.create();

        order.addLine(
                new OrderLine("PRODUCT-1", 2,
                        new BigDecimal("100.00"))
        );

        order.addLine(
                new OrderLine("PRODUCT-2", 3,
                        new BigDecimal("50.00"))
        );

        assertEquals(
                new BigDecimal("350.00"),
                order.total()
        );
    }

    @Test
    void shouldRejectZeroOrNegativeQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderLine(
                        "PRODUCT-1", 0,
                        new BigDecimal("100.00")
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderLine(
                        "PRODUCT-1", -2,
                        new BigDecimal("100.00")
                )
        );
    }

    @Test
    void shouldConfirmOrderAndGenerateEvent() {
        Order order = Order.create();

        order.addLine(
                new OrderLine("PRODUCT-1", 2,
                        new BigDecimal("100.00"))
        );

        order.confirm(NOW);

        assertEquals(OrderStatus.CONFIRMED, order.status());

        List<DomainEvent> events = order.pullDomainEvents();

        assertEquals(1, events.size());
        assertInstanceOf(OrderConfirmed.class, events.get(0));
        assertEquals(order.id(), events.get(0).orderId());
        assertEquals(NOW, events.get(0).occurredAt());

        assertTrue(order.pullDomainEvents().isEmpty());
    }

    @Test
    void shouldRecordPaymentAfterConfirmation() {
        Order order = Order.create();

        order.addLine(
                new OrderLine("PRODUCT-1", 1,
                        new BigDecimal("200.00"))
        );

        order.confirm(NOW);
        order.recordPayment(NOW);

        assertEquals(OrderStatus.PAID, order.status());

        List<DomainEvent> events = order.pullDomainEvents();

        assertEquals(2, events.size());
        assertInstanceOf(OrderConfirmed.class, events.get(0));
        assertInstanceOf(PaymentRecorded.class, events.get(1));
    }

    @Test
    void cancelledOrderCannotBePaid() {
        Order order = Order.create();

        order.addLine(
                new OrderLine("PRODUCT-1", 1,
                        new BigDecimal("100.00"))
        );

        order.cancel(NOW);

        assertEquals(OrderStatus.CANCELLED, order.status());

        assertThrows(
                IllegalStateException.class,
                () -> order.recordPayment(NOW)
        );

        List<DomainEvent> events = order.pullDomainEvents();

        assertEquals(1, events.size());
        assertInstanceOf(OrderCancelled.class, events.get(0));
    }

    @Test
    void shouldNotAllowAddingLinesAfterConfirmation() {
        Order order = Order.create();

        order.addLine(
                new OrderLine("PRODUCT-1", 1,
                        new BigDecimal("100.00"))
        );

        order.confirm(NOW);

        assertThrows(
                IllegalStateException.class,
                () -> order.addLine(
                        new OrderLine("PRODUCT-2", 1,
                                new BigDecimal("50.00"))
                )
        );
    }

    @Test
    void shouldNotAllowPayingDraftOrder() {
        Order order = Order.create();

        assertThrows(
                IllegalStateException.class,
                () -> order.recordPayment(NOW)
        );
    }
}
