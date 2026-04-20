# J-Commerce: Project Overview

## 🎯 What is J-Commerce?

J-Commerce is a **microservices-based e-commerce platform** built with Java and Spring Boot. It's a personal project designed to demonstrate modern enterprise architecture patterns.

The system handles the complete e-commerce lifecycle:
- 👤 User authentication and profiles
- 📦 Product catalog and inventory
- 🛒 Shopping cart and orders
- 💳 Payment processing
- 🔔 User notifications

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Caddy (API Gateway)                      │
│              Reverse Proxy + Load Balancing                 │
└──────────────┬──────────────────────────────────────────────┘
               │
        ┌──────┴──────┬──────────┬──────────┬──────────┐
        │             │          │          │          │
    ┌───▼───┐     ┌──▼──┐  ┌───▼──┐  ┌───▼──┐  ┌───▼────┐
    │Identity   │Product│  │Order │  │Payment│  │Notif.  │
    │Service    │Service │  │Service │  │Service │  │Service │
    └───┬───┘     └──┬──┘  └───┬──┘  └───┬──┘  └───┬────┘
        │             │         │         │         │
    ┌───▼───┐     ┌──▼──┐  ┌───▼──┐  ┌───▼──┐     │
    │ID DB  │     │Prod │  │Order │  │Shared│     │
    │(PG)   │     │DB(PG)   │DB(PG)   │DB(PG)     │
    └───────┘     └──────┘  └───────┘  └───────┘   │
                                                    │
                    ┌──────────────────────────────┤
                    │                              │
                ┌───▼────┐                  ┌─────▼──┐
                │RabbitMQ│◄────────────────►│ Redis  │
                │(Events)│                  │(Cache) │
                └─────────┘                 └────────┘

        ┌────────────────────────────────────┐
        │    Observability                   │
        │ Prometheus + Grafana + Application │
        │          Metrics                   │
        └────────────────────────────────────┘
```

---

## 🔒 Distributed Authentication

J-Commerce uses **JWT tokens with asymmetric cryptography (ECC)** for distributed authentication:

### How It Works

1. **Authentication Server** (Identity Service):
   - Validates user credentials
   - Generates and **signs JWT tokens** with private key (ECC)
   - Exposes public key at `/.well-known/jwks.json`

2. **Resource Servers** (Other services):
   - Receive JWT tokens in Authorization header
   - **Validate signature** using public key (doesn't need private key)
   - Extract user info from token claims
   - Allow/deny access based on claims

### Security Benefits

- ✅ Only Identity Service has private key (single source of truth for signing)
- ✅ Public key is shared with all services (anyone can verify)
- ✅ Tokens cannot be forged (signature would be invalid)
- ✅ Tokens cannot be modified (hash would break signature)
- ✅ No need for distributed cache for token validation

### Implementation

```java
// Identity Service: Generate token
JwtTokenProvider.generateToken(userId, roles)  // Signed with private key

// Other Services: Validate token
JwtTokenProvider.validateToken(token)  // Verified with public key
```

---

## 🌉 Inter-Service Communication

### Synchronous (HTTP/REST)

Used for immediate, request-response scenarios:

```java
// Example: Order service validates product availability
RestTemplate.get("https://product-service/api/v1/products/{id}")
```

### Asynchronous (Message Queue)

Used for events and deferred processing:

```java
// Example: Profile created → Notification published
applicationEventPublisher.publishEvent(new ProfileCreatedEvent(userId))
```

The event is published to RabbitMQ, consumed by:
- ✉️ Notification Service (sends welcome email)
- 📊 Analytics Service (future - tracks signups)

---

## 📊 Technology Stack

| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| **Framework** | Spring Boot | 4.0.5 | Application framework |
| **Language** | Java | 21 | Runtime |
| **Database** | PostgreSQL | 17 | Persistent data storage |
| **Cache** | Redis | 7.4 | Session & performance caching |
| **Message Queue** | RabbitMQ | 4 | Async inter-service communication |
| **API Gateway** | Caddy | 2.11 | Reverse proxy & routing |
| **Monitoring** | Prometheus | 3.1.0 | Metrics collection |
| **Dashboards** | Grafana | 11.5.0 | Metrics visualization |
| **Container** | Docker | - | Containerization |

---

## 📦 Microservices

### 1. **Identity Service** (`microservice-identity/`)
- **Purpose**: Authentication & user account management
- **Database**: PostgreSQL (identity_db)
- **Key Features**:
  - User registration & login
  - JWT token generation (private key signing)
  - Role-based access control (RBAC)
  - User profile management
- **Events Published**: ProfileCreated, ProfileUpdated
- **API Base**: `/identity/api/v1`

### 2. **Product Service** (`microservice-product/`)
- **Purpose**: Product catalog, inventory & wishlist
- **Database**: PostgreSQL (product_db)
- **Key Features**:
  - Product browsing & filtering
  - Stock level management
  - Category management
  - Wishlist management
- **Events Published**: ProductStockChanged, ProductCreated
- **API Base**: `/product/api/v1`

### 3. **Order Service** (`microservice-order/`)
- **Purpose**: Shopping cart & order lifecycle
- **Database**: PostgreSQL (order_db)
- **Key Features**:
  - Shopping cart management
  - Order creation & tracking
  - Order status management
  - Order history
- **Events Published**: OrderCreated, OrderStatusChanged
- **API Base**: `/order/api/v1`

### 4. **Payment Service** (`microservice-payment/`)
- **Purpose**: Payment processing & transaction management
- **Database**: Shared with Identity (currently)
- **Key Features**:
  - Payment method management
  - Transaction processing
  - Refund handling
  - Payment status tracking
- **Events Published**: PaymentProcessed, PaymentFailed
- **API Base**: `/payment/api/v1`

### 5. **Notification Service** (`microservice-notification/`)
- **Purpose**: User notifications management
- **Database**: Currently stateless
- **Key Features**:
  - Email notifications
  - Event subscriptions
  - Notification history
  - Preference management
- **Events Consumed**: ProfileCreated, OrderCreated, PaymentProcessed
- **API Base**: `/notification/api/v1`

---

## 🌐 API Gateway (Caddy)

Routes incoming requests to appropriate microservices:

```
https://localhost/identity/*   → Identity Service (8080)
https://localhost/product/*    → Product Service (8080)
https://localhost/order/*      → Order Service (8080)
https://localhost/payment/*    → Payment Service (8080)
https://localhost/notification/* → Notification Service (8080)
```

All services run on port 8080 internally, Caddy handles external routing.

---

## 📈 Data Flow Examples

### User Registration

```
1. Client POST /identity/api/v1/auth/register
   ↓
2. Identity Service validates & creates user
   ↓
3. Publishes "ProfileCreated" event
   ↓
4. Notification Service receives event
   ↓
5. Sends welcome email
   ↓
6. Client receives 201 Created
```

### Product Purchase

```
1. Client GET /product/api/v1/products/{id}
   ↓
2. Product Service returns product data & stock
   ↓
3. Client POST /order/api/v1/orders
   ↓
4. Order Service validates, creates order
   ↓
5. Order Service calls Payment Service
   ↓
6. Payment Service processes payment
   ↓
7. If successful: "OrderCreated" event published
   ↓
8. Notification Service sends order confirmation
```

---

## 🔄 Service Dependencies

```
Identity Service
  ├─ (no direct dependencies)
  └─ Publishes: ProfileCreated, ProfileUpdated

Product Service
  ├─ Calls: Identity (via JWT validation)
  └─ Publishes: ProductStockChanged

Order Service
  ├─ Calls: Identity, Product
  ├─ Calls: Payment
  └─ Publishes: OrderCreated, OrderStatusChanged

Payment Service
  ├─ Calls: Identity
  └─ Publishes: PaymentProcessed, PaymentFailed

Notification Service
  ├─ Consumes: All published events
  └─ (no outgoing calls)
```

---

## 📊 Database Strategy

### Database-per-Service Pattern

Each microservice has its own **dedicated PostgreSQL database**:

```
identity_db  (port 5432)
product_db   (port 5433)
order_db     (port 5434)
```

**Benefits:**
- ✅ Service independence (can scale separately)
- ✅ Different schemas per domain
- ✅ Service can evolve database independently
- ✅ Reduces coupling

**Trade-offs:**
- ⚠️ No traditional foreign keys across services
- ⚠️ Distributed transactions complex
- ⚠️ Requires event-driven consistency

---

## 🔌 Integration Points

### When Services Need to Communicate

**Immediate Response Needed** → HTTP/REST
- Example: Order service checks product availability

**Fire-and-Forget** → RabbitMQ Events
- Example: Profile created → Send welcome email

**Shared Cache** → Redis
- Sessions, frequently accessed data

---

## 🚀 Deployment Overview

All services are containerized and orchestrated via Docker Compose:

```bash
# Start entire stack
docker-compose up -d

# Each service:
# - Builds from Dockerfile
# - Connects to its database
# - Joins RabbitMQ network
# - Routes through Caddy
```

---

## 📝 Development Workflow

```
1. Create feature branch
2. Modify code in target microservice
3. Test locally (docker-compose up)
4. Commit with meaningful message
5. Create pull request
6. Code review
7. Merge to main
```

---

## 🎓 Key Concepts to Know

- **Bounded Context**: Each service owns its domain (DDD)
- **Event Sourcing**: Services publish events for cross-service updates
- **Idempotency**: Services should handle duplicate events gracefully
- **Circuit Breaker**: Resilience pattern for HTTP calls (future)
- **CQRS**: Command-Query Responsibility Segregation (optional future)

---

**Status**: 🚧 In Development | **Last Updated**: April 2026
