# J-Commerce: Architecture Patterns

This guide explains the architectural patterns and best practices used throughout J-Commerce.

---

## 🏗️ Layered Architecture (Clean Architecture)

Every microservice follows a **4-layer clean architecture**:

```
┌─────────────────────────────────────┐
│  Infra Layer (HTTP, Persistence)    │ ← Frameworks, External Services
├─────────────────────────────────────┤
│  Application Layer (Use Cases)      │ ← Business Workflows, DTOs
├─────────────────────────────────────┤
│  Domain Layer (Business Logic)      │ ← Entities, Rules, Aggregates
├─────────────────────────────────────┤
│  External (Frameworks)              │ ← Spring, Jakarta, Lombok
└─────────────────────────────────────┘
```

### Directory Structure

```
microservice-{name}/
├── src/main/java/com/{service}/
│   ├── {DomainName}/
│   │   ├── domain/
│   │   │   ├── entity/           # Business entities (JPA)
│   │   │   ├── service/          # Domain services (optional)
│   │   │   └── repository/       # Interfaces only
│   │   ├── application/
│   │   │   ├── service/          # Use case implementations
│   │   │   ├── dto/              # Data transfer objects
│   │   │   ├── exception/        # Domain-specific exceptions
│   │   │   ├── event/            # Application events
│   │   │   └── handler/          # Event handlers, mappers
│   │   ├── infra/
│   │   │   ├── web/              # REST controllers
│   │   │   └── persistence/      # Repository implementations
│   │   └── config/               # Spring configuration
│   ├── common/
│   │   ├── handler/              # Global exception handlers
│   │   ├── dto/                  # Shared DTOs
│   │   └── event/                # Shared events
│   └── MicroserviceApplicationMain.java
└── resources/
    ├── application.yaml          # Production config
    ├── application-dev.yaml      # Development config
    ├── application-test.yaml     # Test config
    └── db/                       # Database migrations
```

---

## 📚 Layer Responsibilities

### Domain Layer

**Purpose**: Pure business logic, independent of frameworks

**Contains:**
- Entities with business rules
- Domain events
- Repository interfaces
- Domain services (optional, for complex logic)

**Key Rules:**
- ✅ NO Spring annotations (except @Entity, @Table)
- ✅ NO external dependencies
- ✅ Self-contained and testable
- ✅ Implements business rules & constraints
- ✅ **Soft Delete by Default**: All entities must use soft delete via `isActive` boolean field. Never use hard delete (e.g., `repository.delete()`). Set `entity.setActive(false)` instead. Queries should filter out inactive records by default.

**Example:**

```java
@Entity @Table(name = "product")
@Data @NoArgsConstructor @AllArgsConstructor
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank
    private String name;
    
    @NotNull
    private BigDecimal price;
    
    private int stockLevel;
    
    // Business method - implements rule
    public boolean canPurchase(int quantity) {
        return stockLevel >= quantity && price.signum() > 0;
    }
    
    public void reduceStock(int quantity) {
        if (!canPurchase(quantity)) {
            throw new InsufficientStockException();
        }
        this.stockLevel -= quantity;
    }
}
```

### Application Layer

**Purpose**: Orchestrate domain objects to implement use cases

**Contains:**
- Services with business workflows
- Data Transfer Objects (DTOs)
- Application exceptions
- Event publishers
- Mappers

**Key Rules:**
- ✅ Uses domain objects
- ✅ Orchestrates workflows
- ✅ Handles transactions
- ✅ Publishes events
- ✅ NO HTTP/database details

**Example:**

```java
@Service
public class AuthService {
    private final UserCredentialsRepository repository;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider tokenProvider;
    
    @Transactional
    public void register(RegisterRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException();
        }
        
        UserCredentials user = new UserCredentials();
        user.setEmail(request.email());
        user.setPassword(encoder.encode(request.password()));
        user.setRole(UserRole.CUSTOMER);
        
        repository.save(user);
        
        applicationEventPublisher.publishEvent(
            new ProfileCreatedEvent(user.getId())
        );
    }
}
```

### Infra Layer (Web)

**Purpose**: Handle HTTP requests/responses

**Contains:**
- REST Controllers
- Request validation
- HTTP status codes
- Response formatting

**Key Rules:**
- ✅ Minimal logic (delegate to services)
- ✅ Handle HTTP concerns only
- ✅ Validate @RequestBody with @Valid
- ✅ Return proper status codes

**Example:**

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @PostMapping("/login")
    public ResponseEntity<StandardResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(StandardResponse.success(response));
    }
}
```

### Infra Layer (Persistence)

**Purpose**: Database access

**Contains:**
- Repository implementations (extends JpaRepository)
- Custom queries
- Database initialization

**Key Rules:**
- ✅ Implements domain repository interfaces
- ✅ Only database operations
- ✅ No business logic
- ✅ **Soft Delete Only**: Never use hard delete (`repository.delete()`). Use `entity.setActive(false)` instead.
- ✅ **Filter by Default**: All find queries should manually filter `WHERE is_active = true` (no automatic filtering).

**Example:**

```java
@Repository
public interface UserCredentialsRepository extends JpaRepository<UserCredentials, UUID> {
    Optional<UserCredentials> findByEmail(String email);
    boolean existsByEmail(String email);
    
    // Explicit query for inactive records (admin use only)
    @Query("SELECT u FROM UserCredentials u WHERE u.email = :email AND u.isActive = false")
    Optional<UserCredentials> findInactiveByEmail(String email);
}
```

**Soft Delete Entity Example:**

```java
@Entity
@Table(name = "users")
@Data @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id
    private UUID id;
    
    private String email;
    
    @Column(name = "is_active")
    private Boolean isActive = true;  // Soft delete field (Boolean, not boolean)
    
    public void softDelete() {
        this.setActive(false);
    }
}
```

**Important**: Use `Boolean` (wrapper class), not `boolean` primitive. This allows distinguishing between "not yet set" and "false".

---

## 🎭 Domain-Driven Design (DDD)

J-Commerce follows DDD principles:

### Bounded Contexts

Each microservice is a **bounded context**:

```
Identity Service (Bounded Context)
├── Aggregate: UserCredentials
│   ├── Entity: UserRole
│   └── Entity: RefreshToken
└── Aggregate: UserProfile

Product Service (Bounded Context)
├── Aggregate: Product
│   ├── Entity: ProductSKU
│   ├── Entity: ProductCategory
│   └── Value Object: Price
├── Aggregate: Wishlist
└── Aggregate: Inventory

Order Service (Bounded Context)
├── Aggregate: Order
│   ├── Entity: OrderItem
│   └── Entity: OrderStatus
└── Aggregate: ShoppingCart
```

### Aggregates

An aggregate is a group of related entities treated as a single unit:

```java
// Product is the aggregate root
@Entity
public class Product {
    @Id private UUID id;
    
    // Other Product entities are accessed through Product
    @OneToMany
    private List<ProductSKU> skus;
    
    @ManyToOne
    private ProductCategory category;
}

// External code interacts with Product, not ProductSKU directly
product.reduceStock(quantity);  // ✅ Through aggregate root
productSKU.reduceStock(quantity);  // ❌ Avoid direct access
```

### Value Objects

Immutable objects that represent a concept:

```java
@Embeddable
@Value
public class Money {
    @Column(name = "amount", precision = 19, scale = 2)
    private final BigDecimal amount;
    
    @Column(name = "currency")
    private final String currency;
    
    public Money add(Money other) {
        return new Money(amount.add(other.amount), currency);
    }
}

// Usage
@Entity
public class Product {
    @Embedded
    private Money price;
}
```

---

## 🔄 Event-Driven Architecture

### Publishing Events

Events represent **something that happened** in the past:

```java
@Service
public class AuthService {
    private final ApplicationEventPublisher eventPublisher;
    
    public void register(RegisterRequest request) {
        // ... business logic ...
        
        // Publish event - what happened
        eventPublisher.publishEvent(
            new ProfileCreatedEvent(userId, email)
        );
    }
}
```

### Event Definition

```java
@Getter
@EqualsAndHashCode(callSuper = false)
public class ProfileCreatedEvent extends ApplicationEvent {
    private final UUID userId;
    private final String email;
    
    public ProfileCreatedEvent(Object source, UUID userId, String email) {
        super(source);
        this.userId = userId;
        this.email = email;
    }
}
```

### Event Handling (Same Service)

```java
@Component
public class ProfileEventListener {
    
    @EventListener(ProfileCreatedEvent.class)
    public void onProfileCreated(ProfileCreatedEvent event) {
        // Handle synchronously
        initializeDefaultPreferences(event.getUserId());
    }
}
```

### Event Handling (Different Service - RabbitMQ)

```java
@Component
public class NotificationEventListener {
    
    @RabbitListener(queues = "profile.created")
    public void onProfileCreated(ProfileCreatedEvent event) {
        emailService.sendWelcomeEmail(event.getEmail());
    }
}
```

### Event Publishing to RabbitMQ

Configure exchange & binding:

```java
@Configuration
public class RabbitMQConfig {
    
    @Bean
    public Exchange profileExchange() {
        return new TopicExchange("profile.exchange", true, false);
    }
    
    @Bean
    public Queue profileCreatedQueue() {
        return new Queue("profile.created", true);
    }
    
    @Bean
    public Binding profileCreatedBinding() {
        return BindingBuilder
            .bind(profileCreatedQueue())
            .to(profileExchange())
            .with("profile.created.*");
    }
}
```

Then publish:

```java
@Service
public class AuthService {
    private final RabbitTemplate rabbitTemplate;
    
    public void register(RegisterRequest request) {
        // ... register logic ...
        
        rabbitTemplate.convertAndSend(
            "profile.exchange",
            "profile.created.new",
            new ProfileCreatedEvent(source, userId, email)
        );
    }
}
```

---

## 🔐 Communication Patterns

### Pattern 1: Synchronous HTTP (Request-Response)

**When**: Need immediate response

```java
// Order Service checking product availability
@Service
public class OrderService {
    private final RestTemplate restTemplate;
    
    public void createOrder(CreateOrderRequest request) {
        // Call Product Service synchronously
        ProductDTO product = restTemplate.getForObject(
            "https://product-service/api/v1/products/{id}",
            ProductDTO.class,
            request.productId()
        );
        
        if (product.stockLevel() < request.quantity()) {
            throw new InsufficientStockException();
        }
    }
}
```

**Pros:**
- Simple to implement
- Immediate feedback
- Easy debugging

**Cons:**
- Creates coupling
- Service B must be available
- Slower than async

### Pattern 2: Asynchronous Events (Fire-and-Forget)

**When**: Event happened, other services should know

```java
// Identity Service publishes event
eventPublisher.publishEvent(new ProfileCreatedEvent(...));

// Later, consuming services react
@RabbitListener(queues = "profile.created")
public void onProfileCreated(ProfileCreatedEvent event) {
    // Send email, log analytics, etc
}
```

**Pros:**
- Loose coupling
- Services don't need to know about each other
- Resilient (if consumer down, message queued)
- Better performance

**Cons:**
- Eventual consistency
- Harder to debug
- No immediate response

---

## 💾 Data Transfer Objects (DTOs)

### Request DTOs

Validate and transform client input:

```java
public record LoginRequest(
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    String email,
    
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password
) {}
```

### Response DTOs

Transform entities before returning to client:

```java
public record LoginResponse(
    String accessToken,
    String refreshToken,
    Long expiresIn,
    UserDTO user
) {}

public record UserDTO(
    UUID id,
    String email,
    String role,
    Instant createdAt
) {}
```

### Mapper Pattern

```java
@Component
public class UserMapper {
    public UserDTO toDTO(UserCredentials entity) {
        return new UserDTO(
            entity.getId(),
            entity.getEmail(),
            entity.getRole().name(),
            entity.getCreatedAt()
        );
    }
    
    public UserCredentials toEntity(CreateUserRequest request) {
        UserCredentials user = new UserCredentials();
        user.setEmail(request.email());
        user.setPassword(request.password());
        return user;
    }
}
```

---

## ⚠️ Exception Handling

### Custom Exceptions

Create service-specific exceptions:

```java
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("User with email " + email + " already exists");
    }
}

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(UUID productId, int required, int available) {
        super(String.format(
            "Product %s: required %d, available %d",
            productId, required, available
        ));
    }
}
```

### Global Exception Handler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
        UserAlreadyExistsException ex) {
        
        log.warn("User already exists: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(
                "USER_ALREADY_EXISTS",
                ex.getMessage()
            ));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(
        MethodArgumentNotValidException ex) {
        
        String message = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("VALIDATION_ERROR", message));
    }
}
```

---

## 🔐 Security Architecture

### JWT Token Flow

```
1. Client POST /auth/register
   ↓
2. Identity Service creates user
   ↓
3. Client POST /auth/login with credentials
   ↓
4. Identity Service validates & generates JWT
   - Signs with private key (ECC)
   - Includes: user_id, email, roles, exp
   ↓
5. Returns JWT to client
   ↓
6. Client sends JWT in Authorization: Bearer <token>
   ↓
7. Other services validate JWT
   - Uses public key
   - Verifies signature
   - Checks expiration
```

### JWT Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
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

---

## 📊 Standard Response Format

All API responses follow a consistent structure:

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGc...",
    "refreshToken": "eyJ0eXAiOiJKV1QiLCJhbGc...",
    "expiresIn": 3600,
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "user@example.com",
      "role": "CUSTOMER"
    }
  },
  "timestamp": "2026-04-14T12:30:00Z"
}
```

```java
public record StandardResponse<T>(
    boolean success,
    T data,
    String message,
    Instant timestamp
) {
    public static <T> StandardResponse<T> success(T data) {
        return new StandardResponse<>(true, data, null, Instant.now());
    }
    
    public static StandardResponse<?> error(String message) {
        return new StandardResponse<>(false, null, message, Instant.now());
    }
}
```

---

## 🧪 Testing Patterns

*(Not covered in this guide per your request)*

---

**Status**: 🚧 In Development | **Last Updated**: April 2026
