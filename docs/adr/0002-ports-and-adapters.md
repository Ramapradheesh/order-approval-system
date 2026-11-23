
# ADR 0002: Ports and Adapters Architecture

## Status
Accepted

## Context

The Order Approval System requires persistence, notifications,
and time-related functionality.

The business logic should not depend directly on databases,
notification providers, or system clock implementations.

The application must also be easy to test using in-memory
implementations.

## Decision

We selected Ports and Adapters (Hexagonal Architecture).

The Application module defines the following ports:

1. OrderRepository
   - Saves orders.
   - Retrieves orders by ID.

2. NotificationPort
   - Publishes domain events.

3. TimeProvider
   - Supplies the current timestamp.

The Infrastructure module provides implementations:

1. InMemoryOrderRepository
2. InMemoryNotificationAdapter
3. SystemTimeProvider

The OrderApplicationService coordinates the domain model
through these interfaces.

## Consequences

### Benefits

- Business logic is independent of infrastructure details.
- Adapters can be replaced without changing domain rules.
- In-memory implementations support automated testing.
- Dependencies are directed toward abstractions.

### Trade-offs

- More interfaces and classes are required.
- Dependency wiring must be configured.
- Additional testing is needed for adapter implementations.

## Alternatives Considered

1. Direct database access from domain classes.
2. Framework-dependent service and repository classes.
3. Ports and Adapters architecture.

Ports and Adapters was selected because it keeps business
rules independent of external technologies.

## References

- Hexagonal Architecture
- Dependency Inversion Principle
- Java 17
- Maven Multi-Module Architecture
