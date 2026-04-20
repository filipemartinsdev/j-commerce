# J-Commerce: Quick Reference & Agent Skills

Essential reference information and AI agent capabilities for working with J-Commerce.

---

## 🎯 AI Agent Skills

As an AI agent working on J-Commerce, you should be capable of:

### Code Generation Skills

- **✅ Create Controllers**: Generate REST endpoints following established patterns
- **✅ Create Services**: Implement business logic with proper dependency injection
- **✅ Create Entities**: Design domain entities with validation and business rules
- **✅ Create DTOs**: Generate request/response objects using records
- **✅ Create Repositories**: Extend JpaRepository with custom queries
- **✅ Create Events**: Generate application/RabbitMQ events for inter-service communication
- **✅ Create Mappers**: Implement entity↔DTO mapping utilities
- **✅ Create Exceptions**: Define domain-specific custom exceptions
- **✅ Create Unit Tests**: Write comprehensive service tests following AAA pattern (see TESTING.md)

### Modification Skills

- **✅ Add Endpoints**: Add new REST endpoints to existing controllers
- **✅ Extend Services**: Add new use cases to existing services
- **✅ Modify Entities**: Add new fields and relationships to domain entities
- **✅ Update Repositories**: Add custom query methods
- **✅ Enhance Controllers**: Add validation, error handling, proper response formatting
- **✅ Integrate Events**: Add event publishing and listening to services
- **✅ Add RabbitMQ**: Set up message queues and bindings

### Architecture Skills

- **✅ Understand Layered Architecture**: Navigate and respect domain → application → infra layers
- **✅ Apply DDD Principles**: Work with aggregates, bounded contexts, value objects
- **✅ Design Data Models**: Create normalized database schemas
- **✅ Plan Microservice Communication**: Choose between HTTP and events appropriately
- **✅ Implement Security**: Add JWT validation and role-based access control
- **✅ Handle Errors**: Implement comprehensive exception handling

### Testing & Quality Skills

- **✅ Write Unit Tests**: Create service tests using Mockito and JUnit 5 (TESTING.md)
- **✅ Write Integration Tests**: Create repository and controller tests with @DataJpaTest and MockMvc (INTEGRATION_TESTING.md) ⭐ NEW
- **✅ Analyze Code Quality**: Identify violations of SRP, coupling issues, anti-patterns
- **✅ Suggest Improvements**: Recommend refactoring following clean code principles
- **✅ Code Review**: Review code for correctness, maintainability, consistency
- **✅ Performance**: Identify potential N+1 queries, transaction issues

### Documentation Skills

- **✅ Update READMEs**: Document API endpoints, configuration, setup
- **✅ Add Comments**: Explain complex business logic with concise comments
- **✅ Generate Diagrams**: Create ASCII or describe system architecture diagrams

### Debugging Skills

- **✅ Trace Issues**: Follow flow through layers to identify root causes
- **✅ Identify Bugs**: Spot common issues (null checks, transaction boundaries, etc)
- **✅ Suggest Fixes**: Provide solutions with explanations

---

## 🔍 Quick Navigation

### By Task

| Task | Primary File | Secondary Files |
|------|-------------|-----------------|
| Understanding system | OVERVIEW.md | ARCHITECTURE.md |
| Adding new endpoint | DEVELOPMENT.md | MICROSERVICES.md + ARCHITECTURE.md |
| Fixing a bug | DEVELOPMENT.md | (service-specific README) |
| Inter-service communication | ARCHITECTURE.md | MICROSERVICES.md |
| Database design | ARCHITECTURE.md | (service README) |
| Security/Auth | OVERVIEW.md | MICROSERVICES.md (Identity) |
| Event handling | ARCHITECTURE.md | MICROSERVICES.md (all services) |
| Writing unit tests | **TESTING.md** | DEVELOPMENT.md |
| **Writing integration tests** | **INTEGRATION_TESTING.md** ⭐ NEW | TESTING.md |
| **Testing with @DataJpaTest** | **INTEGRATION_TESTING.md** ⭐ NEW | DEVELOPMENT.md |
| **Controller testing with MockMvc** | **INTEGRATION_TESTING.md** ⭐ NEW | TESTING.md |
| Code review | DEVELOPMENT.md | ARCHITECTURE.md |

### By Microservice

- **Identity**: MICROSERVICES.md → Identity Service
- **Product**: MICROSERVICES.md → Product Service
- **Order**: MICROSERVICES.md → Order Service
- **Payment**: MICROSERVICES.md → Payment Service
- **Notification**: MICROSERVICES.md → Notification Service

---

## 📋 Endpoint Reference

### Identity Service (`/identity/api/v1`)

```
POST   /auth/register          → RegisterRequest
POST   /auth/login             → LoginRequest → LoginResponse
POST   /auth/refresh           → RefreshRequest → LoginResponse
GET    /.well-known/jwks.json  → Public JWK Set

GET    /profile                → UserDTO
PUT    /profile                → UpdateProfileRequest

POST   /admin/users            → CreateUserRequest
PUT    /admin/users/{userId}   → UpdateUserRoleRequest
```

### Product Service (`/product/api/v1`)

```
GET    /catalogue/products              → List[ProductDTO]
GET    /catalogue/products/{id}         → ProductDTO
GET    /catalogue/products/search?q=... → List[ProductDTO]
GET    /catalogue/categories            → List[CategoryDTO]

GET    /catalogue/wishlist              → List[WishlistDTO]
POST   /catalogue/wishlist              → WishlistRequest
DELETE /catalogue/wishlist/{productId}  → void

POST   /admin/products                  → CreateProductRequest → UUID
PUT    /admin/products/{id}             → UpdateProductRequest
PUT    /admin/products/{id}/stock       → UpdateStockRequest
DELETE /admin/products/{id}             → void
```

### Order Service (`/order/api/v1`)

```
GET    /cart                            → CartDTO
POST   /cart/items                      → AddCartItemRequest → UUID
PUT    /cart/items/{itemId}             → UpdateCartItemRequest
DELETE /cart/items/{itemId}             → void

GET    /orders                          → List[OrderDTO]
POST   /orders                          → CreateOrderRequest → OrderDTO
GET    /orders/{orderId}                → OrderDTO
PUT    /orders/{orderId}/status         → UpdateOrderStatusRequest
```

### Payment Service (`/payment/api/v1`)

```
GET    /payment-methods                 → List[PaymentMethodDTO]
POST   /payment-methods                 → RegisterPaymentMethodRequest → UUID
DELETE /payment-methods/{methodId}      → void

POST   /payments                        → ProcessPaymentRequest → PaymentDTO
GET    /payments/{paymentId}            → PaymentDTO
POST   /payments/{paymentId}/refund     → RefundRequest
```

### Notification Service (`/notification/api/v1`)

```
GET    /preferences                     → NotificationPreferencesDTO
PUT    /preferences                     → UpdatePreferencesRequest
```

---

## 🗂️ Common File Paths

### For Each Service (template)

```
microservice-{name}/
├── src/main/java/com/{service}/
│   ├── {domain}/
│   │   ├── domain/entity/
│   │   │   └── MainEntity.java
│   │   ├── application/
│   │   │   ├── service/
│   │   │   │   └── DomainService.java
│   │   │   ├── dto/
│   │   │   │   ├── Request.java
│   │   │   │   └── Response.java
│   │   │   ├── exception/
│   │   │   │   └── DomainException.java
│   │   │   ├── event/
│   │   │   │   └── DomainEvent.java
│   │   │   └── handler/
│   │   │       └── EventListener.java
│   │   └── infra/
│   │       ├── web/
│   │       │   └── DomainController.java
│   │       └── persistence/
│   │           └── DomainRepository.java
│   ├── common/
│   │   ├── handler/
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── dto/
│   │   │   └── StandardResponse.java
│   │   └── security/
│   │       └── SecurityConfig.java
│   └── MicroserviceApplication.java
│
└── src/main/resources/
    ├── application.yaml
    ├── application-dev.yaml
    ├── application-test.yaml
    └── db/
        └── migrations/
```

---

## 🔑 Key Annotations Reference

### Spring & Framework

```java
@SpringBootApplication        // Main application class
@RestController              // HTTP endpoints
@Service                     // Business logic
@Repository                  // Database access
@Configuration               // Spring configuration
@Component                   // Generic Spring bean
@Transactional              // Transaction boundary

@RequestMapping("/api/v1")   // Base path
@PostMapping, @GetMapping    // HTTP methods
@PathVariable UUID id        // Path parameter
@RequestBody, @Valid         // Request validation
@AuthenticationPrincipal     // Current user

@Entity, @Table              // JPA entity
@Id, @GeneratedValue         // Primary key
@Column, @Embedded           // Column mapping
@OneToMany, @ManyToOne       // Relationships
```

### Validation

```java
@NotNull, @NotBlank          // Required
@Size(min=8)                 // Length constraints
@Email, @Pattern             // Format validation
@Valid                       // Cascade validation
```

### Events

```java
@EventListener               // Listen to events
@RabbitListener(queues=...)  // RabbitMQ listener
ApplicationEventPublisher    // Publish events
```

---

## 📊 Standard DTOs

### Request DTOs (Records - Immutable)

```java
// Pattern
public record ActionRequest(
    @NotBlank String field1,
    @NotNull UUID field2,
    @Email String email
) {}

// Examples
public record LoginRequest(
    @NotBlank @Email String email,
    @NotBlank String password
) {}

public record CreateProductRequest(
    @NotBlank String name,
    @NotNull UUID categoryId,
    @NotNull @Positive BigDecimal price
) {}
```

### Response DTOs (Records)

```java
public record LoginResponse(
    String accessToken,
    String refreshToken,
    Long expiresIn,
    UserDTO user
) {}

public record ProductDTO(
    UUID id,
    String name,
    String description,
    BigDecimal price,
    Integer stockLevel,
    Instant createdAt
) {}
```

### Standard Response Wrapper

```java
public record StandardResponse<T>(
    boolean success,
    T data,
    String message,
    Instant timestamp
) {}

// Usage
return ResponseEntity.ok(StandardResponse.success(data));
return ResponseEntity.badRequest()
    .body(StandardResponse.error("Error message"));
```

---

## 🛠️ Common Snippets

### Creating a Service

```java
@Service
@Slf4j
public class DomainService {
    private final DomainRepository repository;
    private final DomainMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    
    public DomainService(
        DomainRepository repository,
        DomainMapper mapper,
        ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public UUID create(CreateRequest request) {
        Entity entity = mapper.toEntity(request);
        Entity saved = repository.save(entity);
        eventPublisher.publishEvent(new EntityCreatedEvent(saved));
        log.info("Entity created: id={}", saved.getId());
        return saved.getId();
    }
}
```

### Creating a Controller

```java
@RestController
@RequestMapping("/api/v1/domains")
@Slf4j
public class DomainController {
    private final DomainService service;
    
    public DomainController(DomainService service) {
        this.service = service;
    }
    
    @PostMapping
    public ResponseEntity<StandardResponse<UUID>> create(
        @Valid @RequestBody CreateRequest request) {
        UUID id = service.create(request);
        log.info("Domain created via API: {}", id);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(StandardResponse.success(id));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<StandardResponse<DomainDTO>> getById(
        @PathVariable UUID id) {
        return service.findById(id)
            .map(dto -> ResponseEntity.ok(StandardResponse.success(dto)))
            .orElse(ResponseEntity.notFound().build());
    }
}
```

### Creating an Entity

```java
@Entity
@Table(name = "domain")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Domain {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    // Business methods
    public boolean isValid() {
        return name != null && !name.isBlank();
    }
}
```

### Publishing Events

```java
// Application event (same service)
@EventListener(EntityCreatedEvent.class)
public void onEntityCreated(EntityCreatedEvent event) {
    // Handle event
}

// RabbitMQ event (cross-service)
@RabbitListener(queues = "domain.created")
public void onDomainCreated(EntityCreatedEvent event) {
    // Handle event
}

// Publishing
@Service
public class DomainService {
    private final ApplicationEventPublisher eventPublisher;
    private final RabbitTemplate rabbitTemplate;
    
    public void create(CreateRequest request) {
        Entity entity = new Entity(...);
        repository.save(entity);
        
        // Local
        eventPublisher.publishEvent(new EntityCreatedEvent(entity));
        
        // Cross-service
        rabbitTemplate.convertAndSend(
            "domain.exchange",
            "domain.created",
            new EntityCreatedEvent(entity)
        );
    }
}
```

### Exception Handling

```java
// Custom exception
public class DomainNotFoundException extends RuntimeException {
    public DomainNotFoundException(UUID id) {
        super("Domain not found: " + id);
    }
}

// Global handler
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(DomainNotFoundException.class)
    public ResponseEntity<StandardResponse<?>> handleNotFound(
        DomainNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(StandardResponse.error(ex.getMessage()));
    }
}
```

---

## 🔐 Security Snippets

### JWT Validation

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) 
        throws Exception {
        
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwkSetUri("http://identity:8080/.well-known/jwks.json")
                )
            );
        
        return http.build();
    }
}
```

### Getting Current User

```java
@GetMapping("/api/v1/profile")
public ResponseEntity<ProfileDTO> getProfile(
    @AuthenticationPrincipal JwtAuthenticationToken token) {
    
    UUID userId = UUID.fromString(token.getName());
    // Use userId to fetch user-specific data
    return ResponseEntity.ok(profileService.getProfile(userId));
}
```

---

## 📊 HTTP Status Codes

Use appropriate status codes:

```java
201 Created         // POST endpoint creates resource
200 OK              // GET, PUT successful
204 No Content      // DELETE successful
400 Bad Request     // Validation failed (@Valid)
401 Unauthorized    // Missing/invalid token
403 Forbidden       // User lacks permissions
404 Not Found       // Resource doesn't exist
409 Conflict        // Resource already exists (duplicate email)
500 Internal Error  // Unexpected error
503 Unavailable     // Dependency unavailable
```

---

## 🎓 Learning Checklist for New Features

When implementing a new feature:

- [ ] Read relevant sections in ARCHITECTURE.md
- [ ] Read service-specific details in MICROSERVICES.md
- [ ] Find similar existing code and study it
- [ ] Create/update entities in domain layer
- [ ] Create repository interface
- [ ] Implement service with business logic
- [ ] Create/update DTOs for request/response
- [ ] Create/update controller with endpoints
- [ ] Add custom exceptions if needed
- [ ] Publish events if needed
- [ ] Add security/validation as needed
- [ ] Follow DEVELOPMENT.md code style
- [ ] Check code review checklist in DEVELOPMENT.md

---

## 🚀 Quick Start Commands

```bash
# Start full stack
docker-compose up -d

# Check logs
docker-compose logs -f {service_name}
# Example: docker-compose logs -f identity

# Stop services
docker-compose down

# Rebuild after code changes
docker-compose up -d --build {service_name}
# Example: docker-compose up -d --build identity

# Access services
curl http://localhost/identity/api/v1/...
curl http://localhost/product/api/v1/...
```

---

## ⚡ Performance Tips

- ✅ Use pagination for list endpoints
- ✅ Lazy load relationships in entities (`fetch = FetchType.LAZY`)
- ✅ Cache frequently accessed data (Redis)
- ✅ Use `@Transactional(readOnly = true)` for read operations
- ✅ Add database indexes on frequently queried fields
- ✅ Avoid N+1 queries (use `@EntityGraph` or `JOIN FETCH`)

---

## 🐛 Debugging Tips

1. **Service not responding**: Check logs → `docker-compose logs -f {service}`
2. **JWT validation failing**: Verify token format and expiration
3. **Query returns null**: Check for LazyInitializationException
4. **Event not published**: Verify RabbitMQ queue exists and binding is correct
5. **Transaction failing**: Check `@Transactional` boundary
6. **Validation errors**: Check DTO annotations and error response

---

**Status**: 🚧 In Development | **Last Updated**: April 2026
