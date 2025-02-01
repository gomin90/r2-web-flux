# R2DBC Reactive Web Application

## System Architecture
```
[Client] ↔ [Spring WebFlux] ↔ [R2DBC] ↔ [PostgreSQL]
```

## Technical Stack
- Java 17
- Spring Boot 3.2.2
- Spring WebFlux
- R2DBC with PostgreSQL
- Spring State Machine
- Event-Driven Architecture
- OpenAPI 3.0 (Springdoc)

## Key Features
1. Reactive Database Operations
   - R2DBC configuration
   - Connection pooling
   - Non-blocking database operations

2. API Documentation
   - OpenAPI 3.0 integration
   - Reactive endpoints documentation

3. State Management
   - Account state management (CREATED, ACTIVE, SUSPENDED, CLOSED)
   - Reactive state transitions
   - Event-driven state changes

4. Event Processing
   - Reactive event handling
   - Asynchronous event processing
   - Event-driven architecture

## API Endpoints
1. Account Management
   - GET /api/v1/accounts - List all accounts (reactive stream)
   - POST /api/v1/accounts - Create new account
   - GET /api/v1/accounts/{id} - Get account details
   - PUT /api/v1/accounts/{id}/state - Update account state

## Implementation Details
1. Configuration
   - R2DBC connection pool setup
   - State machine configuration
   - OpenAPI documentation config

2. Database Schema
   - Account management tables
   - State tracking
   - Event logging

3. Reactive Services
   - Non-blocking account operations
   - Reactive state transitions
   - Event publishing and handling

4. Testing
   - WebTestClient for endpoint testing
   - StepVerifier for reactive streams

5. Response Models
   - AccountCreationResponse
   - AccountUpdateResponse
   - Error handling responses

6. Event System
   - AccountEvent definitions
   - Reactive event listeners
   - State change events
