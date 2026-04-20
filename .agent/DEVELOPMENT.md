# J-Commerce: Development Standards

Guidelines for writing clean, maintainable, consistent code in J-Commerce.

---

## 📝 Code Style & Conventions

### Naming Conventions

**Classes:**
- PascalCase for all classes
- Suffix with domain concepts: `UserService`, `ProductRepository`, `OrderController`

```java
// Controllers
public class AuthController { }
public class ProductCatalogController { }

// Services
public class AuthService { }
public class StockManagementService { }

// Repositories
public interface UserRepository { }
public interface ProductInventoryRepository { }

// Entities
public class UserCredentials { }
public class ProductSKU { }

// DTOs
public record LoginRequest(...) { }
public record ProductResponse(...) { }

// Exceptions
public class UserAlreadyExistsException { }
public class InsufficientStockException { }

// Events
public class ProfileCreatedEvent { }
public class OrderStatusChangedEvent { }
```

**Methods:**
- camelCase
- Verb-noun: `getUser()`, `createOrder()`, `validateEmail()`
- Boolean getters: `isActive()`, `hasPermission()`, `canPurchase()`

```java
public UUID createUser(CreateUserRequest request) { }
public Optional<User> findByEmail(String email) { }
public boolean isValidEmail(String email) { }
public void publishOrderEvent(OrderEvent event) { }
```

**Variables:**
- camelCase
- Descriptive: `userId` not `uid`, `emailAddress` not `em`

```java
UUID userId = UUID.randomUUID();
String emailAddress = "user@example.com";
List<Product> availableProducts = new ArrayList<>();
```

**Constants:**
- UPPER_SNAKE_CASE

```java
public static final long JWT_EXPIRATION_TIME = 3600000L;
public static final String API_BASE_PATH = "/api/v1";
public static final int MAX_PASSWORD_LENGTH = 128;
```

---

## 📦 Package Organization

### Standard Structure

```
com.identity/
├── security/                 # Domain: Authentication
│   ├── domain/
│   │   ├── entity/
│   │   │   ├── UserCredentials.java
│   │   │   ├── UserRole.java
│   │   │   └── RefreshToken.java
│   │   ├── service/         # Domain services (if complex logic)
│   │   └── repository/      # Interfaces only
│   ├── application/
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   └── TokenProvider.java
│   │   ├── dto/
│   │   │   ├── LoginRequest.java
│   │   │   ├── LoginResponse.java
│   │   │   └── TokenResponse.java
│   │   ├── exception/
│   │   │   ├── UserAlreadyExistsException.java
│   │   │   └── InvalidCredentialsException.java
│   │   ├── event/
│   │   │   └── ProfileCreatedEvent.java
│   │   ├── handler/
│   │   │   └── ProfileEventListener.java
│   │   └── mapper/
│   │       └── UserMapper.java
│   ├── infra/
│   │   ├── web/
│   │   │   ├── AuthController.java
│   │   │   ├── AdminController.java
│   │   │   └── UserCredentialsResponse.java
│   │   ├── persistence/
│   │   │   ├── UserCredentialsRepository.java
│   │   │   └── RefreshTokenRepository.java
│   │   └── config/
│   │       └── SecurityConfig.java
│   └── config/
│       └── JwtConfiguration.java
│
├── profile/                  # Domain: User Profile
│   ├── domain/
│   ├── application/
│   ├── infra/
│   └── config/
│
├── common/                   # Cross-cutting concerns
│   ├── handler/
│   │   └── GlobalExceptionHandler.java
│   ├── dto/
│   │   ├── StandardResponse.java
│   │   └── ErrorResponse.java
│   ├── event/
│   │   └── ApplicationEventConfig.java
│   └── security/
│       └── JwtTokenProvider.java
│
└── MicroserviceIdentityApplication.java
```

### Why This Structure?

- ✅ Clear separation of concerns
- ✅ Easy to locate code (predictable path)
- ✅ Follows DDD bounded contexts
- ✅ Scales with service growth
- ✅ Clear layer dependencies

---

## 🎯 Coding Principles

### 1. Single Responsibility Principle

**Each class should have ONE reason to change**

```java
// ❌ BAD: Multiple responsibilities
public class UserService {
    public void createUser(CreateUserRequest request) {
        // Validation
        if (!isValidEmail(request.email())) throw new Exception(...);
        
        // Password hashing (why is this here?)
        String hashed = bcrypt.hash(request.password());
        
        // Database persistence (why?)
        database.save(user);
        
        // Email sending (why?)
        emailService.send(...);
    }
}

// ✅ GOOD: Each has single responsibility
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final ApplicationEventPublisher eventPublisher;
    
    public void createUser(CreateUserRequest request) {
        UserCredentials user = new UserCredentials(
            request.email(),
            encoder.encode(request.password())
        );
        
        repository.save(user);
        eventPublisher.publishEvent(new ProfileCreatedEvent(user));
    }
}

@Component
public class UserValidator {
    public void validate(CreateUserRequest request) {
        if (!isValidEmail(request.email())) {
            throw new InvalidEmailException(request.email());
        }
    }
}

@Component
public class WelcomeEmailSender {
    @EventListener(ProfileCreatedEvent.class)
    public void sendWelcomeEmail(ProfileCreatedEvent event) {
        emailService.send(event.getEmail());
    }
}
```

### 2. Dependency Injection

**Always inject dependencies through constructor**

```java
// ❌ BAD: Service locator pattern
public class OrderService {
    private UserRepository userRepository = ServiceLocator.get(UserRepository.class);
    
    public void createOrder(CreateOrderRequest request) {
        // Tightly coupled, hard to test
    }
}

// ✅ GOOD: Constructor injection
public class OrderService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    
    public OrderService(
        UserRepository userRepository,
        ProductRepository productRepository,
        OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }
    
    public void createOrder(CreateOrderRequest request) {
        // Easy to test - pass mocks
    }
}
```

### 3. Don't Return Null

**Use Optional or exceptions instead**

```java
// ❌ BAD: Null returns
public User findById(UUID id) {
    return repository.findById(id).orElse(null);  // Could be null!
}

public void processUser(UUID id) {
    User user = findById(id);
    if (user != null) {  // ← Defensive check everywhere
        user.process();
    }
}

// ✅ GOOD: Use Optional
public Optional<User> findById(UUID id) {
    return repository.findById(id);
}

public void processUser(UUID id) {
    findById(id)
        .ifPresent(User::process);
}

// ✅ ALSO GOOD: Throw exception for required entities
public User getUserOrThrow(UUID id) {
    return repository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
}
```

### 4. Immutability Where Possible

**Use records and final fields**

```java
// ❌ BAD: Mutable DTO
public class UserDTO {
    private String email;
    private String name;
    
    public void setEmail(String email) { this.email = email; }  // ← Can be modified
}

// ✅ GOOD: Immutable record
public record UserDTO(
    String email,
    String name,
    UUID id
) {}

// ✅ GOOD: Immutable entity (use getters, business methods)
@Entity
public class User {
    @Id private UUID id;
    private String email;
    
    // ✅ Allow change through business method
    public void updateEmail(String newEmail) {
        if (!isValidEmail(newEmail)) throw new Exception(...);
        this.email = newEmail;
    }
    
    // ❌ Don't expose setters
    // public void setEmail(String email) { this.email = email; }
}
```

### 5. Fail Fast

**Validate input early**

```java
// ❌ BAD: Silent failure
public void createUser(CreateUserRequest request) {
    if (request.email() == null) {  // ← Silent
        return;
    }
    
    if (request.password().length() < 8) {  // ← Silent
        return;
    }
}

// ✅ GOOD: Fail with clear exceptions
public void createUser(@Valid CreateUserRequest request) {
    // Validation via @Valid and MethodArgumentNotValidException handler
}

// ✅ GOOD: Explicit checks
public void createUser(CreateUserRequest request) {
    if (repository.existsByEmail(request.email())) {
        throw new UserAlreadyExistsException(request.email());
    }
}
```

---

## 📐 Common Code Patterns

### Pattern: Service with Repository Injection

```java
@Service
public class ProductService {
    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    
    public ProductService(
        ProductRepository repository,
        ProductMapper mapper,
        ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public UUID createProduct(CreateProductRequest request) {
        Product product = mapper.toEntity(request);
        Product saved = repository.save(product);
        eventPublisher.publishEvent(new ProductCreatedEvent(saved));
        return saved.getId();
    }
    
    public Optional<ProductDTO> getById(UUID id) {
        return repository.findById(id).map(mapper::toDTO);
    }
}
```

### Pattern: Controller with Service Injection

```java
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService service;
    
    public ProductController(ProductService service) {
        this.service = service;
    }
    
    @PostMapping
    public ResponseEntity<StandardResponse<UUID>> create(
        @Valid @RequestBody CreateProductRequest request) {
        UUID id = service.createProduct(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(StandardResponse.success(id));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<StandardResponse<ProductDTO>> getById(@PathVariable UUID id) {
        return service.getById(id)
            .map(dto -> ResponseEntity.ok(StandardResponse.success(dto)))
            .orElse(ResponseEntity.notFound().build());
    }
}
```

### Pattern: Entity with Business Methods

```java
@Entity
@Table(name = "product")
@Data @NoArgsConstructor @AllArgsConstructor
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank
    private String name;
    
    @NotNull
    private BigDecimal price;
    
    private int stockLevel;
    
    // Business rules
    public boolean canPurchase(int quantity) {
        return stockLevel >= quantity && price.signum() > 0;
    }
    
    public void reduceStock(int quantity) {
        if (!canPurchase(quantity)) {
            throw new InsufficientStockException(id, quantity, stockLevel);
        }
        this.stockLevel -= quantity;
    }
    
    public void replenishStock(int quantity) {
        this.stockLevel += quantity;
    }
}
```

### Pattern: Event Publishing

```java
// Event definition
@Getter
@EqualsAndHashCode(callSuper = false)
public class OrderCreatedEvent extends ApplicationEvent {
    private final UUID orderId;
    private final UUID userId;
    private final List<OrderItem> items;
    
    public OrderCreatedEvent(Object source, UUID orderId, UUID userId, List<OrderItem> items) {
        super(source);
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
    }
}

// Publishing
@Service
public class OrderService {
    private final ApplicationEventPublisher eventPublisher;
    
    public void createOrder(CreateOrderRequest request) {
        // ... create order ...
        eventPublisher.publishEvent(new OrderCreatedEvent(
            this,
            order.getId(),
            order.getUserId(),
            order.getItems()
        ));
    }
}

// Listening (same service)
@Component
public class OrderEventListener {
    @EventListener(OrderCreatedEvent.class)
    public void onOrderCreated(OrderCreatedEvent event) {
        // Publish to RabbitMQ, update cache, etc
    }
}

// Listening (different service via RabbitMQ)
@Component
public class NotificationEventListener {
    @RabbitListener(queues = "order.created")
    public void onOrderCreated(OrderCreatedEvent event) {
        // Send order confirmation email
    }
}
```

---

## 🚫 Anti-Patterns to Avoid

| ❌ Anti-Pattern | ✅ Instead | Why |
|---|---|---|
| Static utility classes everywhere | DI via constructors | Easier to test, change implementations |
| Multiple responsibilities per class | SRP - one reason to change | Maintainability, clarity |
| Returning null | Return Optional or throw | Prevents null pointer exceptions |
| Database logic in controllers | Move to service | Clear separation of concerns |
| No exception handling | Custom exceptions | Clearer error messages, specific handling |
| Mutable DTOs with setters | Records or immutable objects | Prevent accidental changes |
| Logging everywhere | Use AOP or structured logging | Consistency, easier to search |
| Tight coupling between services | Use events and REST | Loose coupling, independent deployment |
| Hardcoded strings/numbers | Use constants or enums | Easier to change, prevents errors |
| Ignoring exceptions | Log and handle appropriately | Helps debugging, user experience |

---

## 🔍 Code Review Checklist

Before submitting code, verify:

- [ ] **Naming**: Clear, follows conventions (PascalCase, camelCase)
- [ ] **SRP**: Class has one reason to change
- [ ] **DI**: Dependencies injected via constructor
- [ ] **Validation**: Input validated early, fails fast
- [ ] **Exceptions**: Custom exceptions for domain errors
- [ ] **Immutability**: DTOs/records are immutable
- [ ] **No nulls**: Using Optional or exceptions
- [ ] **Logging**: Appropriate log levels (debug, info, warn, error)
- [ ] **Documentation**: Complex logic commented
- [ ] **Transactions**: `@Transactional` where needed
- [ ] **DTOs**: Used for request/response (not entities)
- [ ] **HTTP Status**: Appropriate status codes (200, 201, 400, 404, 409, 500)
- [ ] **Standard Response**: Using StandardResponse wrapper
- [ ] **Tests**: Tests written (not verified in this guide)

---

## 📊 Logging Guidelines

**Log Levels:**
- `DEBUG`: Detailed information for debugging (variable values, flow)
- `INFO`: General application flow (service started, user created)
- `WARN`: Potential issues (rate limiting, retry attempt)
- `ERROR`: Errors that might cause issues (exception caught, recovery attempted)

**Good Logging Example:**

```java
@Service
@Slf4j
public class UserService {
    
    public void createUser(CreateUserRequest request) {
        log.debug("Creating user with email: {}", request.email());
        
        if (repository.existsByEmail(request.email())) {
            log.warn("Attempt to create user with existing email: {}", request.email());
            throw new UserAlreadyExistsException(request.email());
        }
        
        UserCredentials user = new UserCredentials(request.email(), ...);
        repository.save(user);
        
        log.info("User created successfully: userId={}", user.getId());
        eventPublisher.publishEvent(new ProfileCreatedEvent(user));
    }
}
```

---

## ✅ Checklist for New Features

1. **Create DTOs** for request/response
2. **Create/update entities** in domain layer
3. **Create repository interface** (if needed)
4. **Implement service** with business logic
5. **Create controller** for HTTP endpoints
6. **Add exception handling** (custom exceptions)
7. **Publish events** (if affects other services)
8. **Configure Spring** (if needed - @Configuration classes)
9. **Validate input** (@Valid, manual checks)
10. **Return proper HTTP status** (201 for creation, 409 for conflicts, etc)

---

**Status**: 🚧 In Development | **Last Updated**: April 2026
