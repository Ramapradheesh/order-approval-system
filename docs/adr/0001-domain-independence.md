
# ADR 0001: Domain Independence

## Status
Accepted

## Context
The Order Approval System must maintain business rules independently
from frameworks, databases, and infrastructure technologies.

The domain model must be testable without Spring, JPA, Hibernate,
or external database dependencies.

## Decision
We use a layered, multi-module Maven architecture with Java 17.

The project contains three modules:

1. Domain
   - Contains Order aggregate.
   - Contains OrderLine value object.
   - Contains OrderStatus enum.
   - Contains domain events.
   - Implements business rules and state transitions.

2. Application
   - Contains OrderApplicationService.
   - Defines OrderRepository port.
   - Defines NotificationPort.
   - Defines TimeProvider.

3. Infrastructure
   - Contains InMemoryOrderRepository.
   - Contains InMemoryNotificationAdapter.
   - Contains SystemTimeProvider.

The domain module has no runtime dependency on application,
infrastructure, or external frameworks.

Dependencies point inward toward the domain model.

## Consequences

### Benefits
- Domain rules remain independent of frameworks.
- Business logic can be tested without a database.
- Infrastructure adapters can be replaced.
- Application services depend on interfaces rather than implementations.

### Trade-offs
- Additional module configuration is required.
- Dependency wiring must be handled outside the domain layer.

## References
- Java 17
- Maven Multi-Module Architecture
- Domain-Driven Design
- Hexagonal Architecture
