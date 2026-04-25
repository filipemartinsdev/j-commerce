# Agent Guidelines

## Project Overview

J-Commerce is a microservice-based e-commerce platform built with Java 21, Spring Boot, PostgreSQL, Redis, and RabbitMQ.

## Service Architecture

### Microservices

1. **microservice-identity**: Authentication, JWT issuance, user management
2. **microservice-product**: Product catalogue, stock, pricing, wishlist, shopping cart
3. **microservice-order**: Order lifecycle, order processing
4. **microservice-payment**: Payment processing
5. **microservice-notification**: User notifications

### Each Service Structure

```
src/main/java/com/{module}/
├── application/
│   ├── dto/           # Data Transfer Objects (records)
│   ├── event/         # Domain events
│   ├── exception/     # Custom exceptions
│   └── service/       # Business logic services
├── domain/
│   └── entity/        # JPA entities
├── infra/
│   ├── persistence/   # JpaRepository interfaces
│   └── web/           # REST controllers
└── config/            # Configuration classes
```

## Authentication Flow

1. Client calls `/api/v1/auth/login` with email/password
2. Identity service validates credentials, issues JWT access + refresh tokens
3. Access token: 1 hour expiry, Refresh token: 14 days expiry
4. Other services validate JWT via JWKS endpoint at `/.well-known/jwks.json`
5. Role scope in JWT determines authorization (SCOPE_ADMIN, SCOPE_USER, etc.)

## Database Patterns

- **Soft Delete**: All entities use `is_active` flag, never hard delete
- **Flyway**: Database migrations in `src/main/resources/db/migration/`
- **UUID**: Primary keys for user-related tables
- **IDENTITY**: Auto-increment for lookup tables (categories, roles)

## API Patterns

### Public Endpoints
- GET `/api/v1/...` - Read operations
- POST `/api/v1/...` - Create operations
- PATCH `/api/v1/...` - Update operations
- DELETE `/api/v1/...` - Soft delete (set is_active=false)

### Admin Endpoints
- GET `/admin/api/v1/...`
- POST `/admin/api/v1/...`
- PATCH `/admin/api/v1/...`
- DELETE `/admin/api/v1/...`

## Common Dependencies

- Spring Web
- Spring Security (OAuth2 Resource Server)
- Spring Data JPA
- Spring Validation
- Spring AMQP (RabbitMQ)
- Flyway
- PostgreSQL Driver
- Lombok

## Error Handling

- Custom exceptions extend RuntimeException
- GlobalExceptionHandler maps exceptions to HTTP responses
- StandardResponse<T> wrapper for all responses

## Event Publishing

- ApplicationEventPublisher for domain events
- Async event handling via @Async or RabbitMQ

## Testing Requirements

All service tests must follow the exact pattern defined in TESTING.md.