# Order Approval System

A Java 17 multi-module application demonstrating Domain-Driven Design and Ports and Adapters architecture.

## Technology Stack
- Java 17
- Apache Maven 3.10.1
- JUnit 5

## Project Modules
- Domain: Order aggregate, OrderLine, OrderStatus and domain events.
- Application: OrderApplicationService and application ports.
- Infrastructure: In-memory repository, notification adapter and system time provider.

## Order States
- DRAFT
- CONFIRMED
- PAID
- CANCELLED

## Business Rules
- An order must contain at least one line before confirmation.
- Quantity must be a positive whole number.
- Cancelled orders cannot be paid.
- Paid orders cannot return to draft.
- Order total is calculated from line prices and quantities.

## Domain Events
- OrderConfirmed
- PaymentRecorded
- OrderCancelled

## Architecture
The project follows Ports and Adapters architecture. The Domain module is independent of frameworks and persistence. The Application module defines ports. The Infrastructure module provides adapters.

## How to Run
Verify Java: `java -version`
Verify Maven: `mvn -version`
Run tests: `mvn test`

## Verified Test Results
| Module | Tests |
|---|---:|
| Domain | 9 |
| Application | 6 |
| Infrastructure | 7 |
| Total | 22 |

All 22 tests passed in the latest verified Maven run.

## Architecture Decision Records
1. 0001-domain-independence.md
2. 0002-ports-and-adapters.md
