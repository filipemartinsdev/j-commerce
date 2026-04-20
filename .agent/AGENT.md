# J-Commerce: AI Agent Development Guide

Welcome! This guide provides everything you need to understand and develop within the J-Commerce microservices architecture.

---

## 📋 Quick Navigation

- **[Project Overview](./OVERVIEW.md)** - Architecture, tech stack, and core concepts
- **[Development Standards](./DEVELOPMENT.md)** - Coding patterns, conventions, and best practices
- **[Architecture Patterns](./ARCHITECTURE.md)** - Layered structure, DDD principles, communication patterns
- **[Microservices Guide](./MICROSERVICES.md)** - Details on each service and their responsibilities
- **[Testing Standards](./TESTING.md)** - Unit testing patterns, AAA pattern, Mockito usage
- **[Integration Testing](./INTEGRATION_TESTING.md)** ⭐ NEW - Repository tests, Controller tests, @DataJpaTest patterns

---

## 🎯 Core Principles

1. **Clean Architecture**: Separation of concerns through domain → application → infra layers
2. **Domain-Driven Design**: Domain logic is independent of frameworks
3. **Microservices**: Each service has its own database and bounded contexts
4. **Event-Driven**: Services communicate asynchronously via RabbitMQ
5. **Security**: JWT-based auth with asymmetric cryptography (ECC)

---

## 📁 Project Structure

```
j-commerce/
├── .agent/                          # AI Agent documentation
├── docker-compose.yaml              # Full stack orchestration
├── Caddyfile                        # Reverse proxy configuration
├── prometheus.yml                   # Metrics configuration
├── grafana.yml                      # Monitoring dashboards
│
├── microservice-identity/           # Authentication & user profiles
├── microservice-product/            # Catalog & inventory
├── microservice-order/              # Order management & shopping cart
├── microservice-payment/            # Payment processing
└── microservice-notification/       # User notifications
```

---

## 🚀 Getting Started

### Prerequisites
- Java 21
- Docker & Docker Compose
- Maven 3.8+

### Running the Stack

```bash
# Copy environment file
cp .example.env .env

# Start all services
docker-compose up -d

# Check status
docker-compose ps
```

Services will be available at:
- Identity: `https://localhost/identity`
- Product: `https://localhost/product`
- Order: `https://localhost/order`
- Payment: `https://localhost/payment`
- Notification: `https://localhost/notification`

---

## 💡 When to Use These Guides

| Task | Guide |
|------|-------|
| Understanding the overall system | OVERVIEW.md |
| Writing new features/controllers/services | DEVELOPMENT.md + ARCHITECTURE.md |
| Adding a new microservice | MICROSERVICES.md |
| Modifying existing service | Service-specific README in MICROSERVICES.md |
| Working with databases/entities | ARCHITECTURE.md (Domain layer) |
| Setting up inter-service communication | ARCHITECTURE.md (Communication section) |
| **Writing unit tests (Services)** | **TESTING.md** |
| **Writing integration tests (Repository/Controller)** | **INTEGRATION_TESTING.md** ⭐ NEW |
| **Understanding @DataJpaTest patterns** | **INTEGRATION_TESTING.md** ⭐ NEW |
| **Understanding test patterns and AAA** | **TESTING.md** |

---

## 🔧 Common Tasks

### Adding a New Endpoint

1. Create a DTO in `application/dto/`
2. Create a Service method in `application/service/`
3. Create a Controller in `infra/web/`
4. **Write unit tests** (see TESTING.md)
5. Document in API docs

**Reference**: Check AuthController in microservice-identity

### Adding a New Entity

1. Create entity in `domain/entity/` with JPA annotations
2. Create repository in `infra/persistence/` (extends JpaRepository)
3. Inject in service if needed
4. Add migrations if needed

**Reference**: Check Product entity in microservice-product

### Publishing an Event

1. Create event class extending ApplicationEvent
2. Publish via ApplicationEventPublisher in service
3. Create event listener in consuming service
4. Publish to RabbitMQ if cross-service

**Reference**: Check ProfileCreatedEvent in microservice-identity

### Writing Unit Tests

1. Create test class in `src/test/` same package as service
2. Mock all dependencies with `@Mock`
3. Inject service with `@InjectMocks`
4. Write tests following AAA pattern (Arrange → Act → Assert)
5. Use Mockito for stubbing and verification

**Reference**: Check AdminProductServiceTests (25 comprehensive tests)

---

## 🎓 Learning Path

**New to the codebase?** Follow this order:

1. Read **OVERVIEW.md** - 15 min
2. Skim **ARCHITECTURE.md** - 20 min
3. Read **DEVELOPMENT.md** - 15 min
4. **Read TESTING.md** - 20 min (Unit testing patterns, Mockito, AAA pattern)
5. **Read INTEGRATION_TESTING.md** - 25 min ⭐ NEW (Repository tests, Controller tests, @DataJpaTest)
6. Pick a microservice from **MICROSERVICES.md** and read its details - 10 min
7. Find similar code in that service and study it
8. Start making small changes and writing tests

**Total Time**: ~115 minutes to understand the full stack and testing patterns

---

## ❓ Help & Questions

- **How do I authenticate in microservices?** → See ARCHITECTURE.md (Distributed Auth section)
- **How do services communicate?** → See ARCHITECTURE.md (Communication Patterns)
- **What's the coding style?** → See DEVELOPMENT.md (Code Style)
- **Where do I add business logic?** → See ARCHITECTURE.md (Domain Layer)
- **How do I write unit tests?** → See TESTING.md (AAA Pattern & Test Patterns)
- **What's a good unit test structure?** → See TESTING.md (AdminProductServiceTests example)
- **How do I test repositories with @DataJpaTest?** → See INTEGRATION_TESTING.md ⭐ NEW
- **Do I need @ActiveProfiles("test")?** → See INTEGRATION_TESTING.md (@DataJpaTest section) ⭐ NEW
- **How do I write controller tests?** → See INTEGRATION_TESTING.md (MockMvc section) ⭐ NEW

---

## 📊 Tech Stack Summary

| Layer | Technology |
|-------|-----------|
| **API Gateway** | Caddy (reverse proxy) |
| **Framework** | Spring Boot 4.0.5 |
| **Runtime** | Java 21 |
| **Database** | PostgreSQL 17 |
| **Cache** | Redis 7.4 |
| **Message Broker** | RabbitMQ 4 |
| **Monitoring** | Prometheus + Grafana |
| **Security** | Spring Security + JWT (ECC) |

---

**Last Updated**: April 2026 | **Project Status**: 🚧 In Development
