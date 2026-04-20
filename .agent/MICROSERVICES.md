# J-Commerce: Microservices Guide

Detailed information about each microservice, their responsibilities, and how to work with them.

---

## 📊 Microservices Overview

| Service | Port | Database | Purpose | Key Entities |
|---------|------|----------|---------|--------------|
| **Identity** | 8080 | identity_db (PG 5432) | Authentication & user accounts | UserCredentials, RefreshToken, UserRole |
| **Product** | 8080 | product_db (PG 5433) | Catalog & inventory | Product, ProductSKU, ProductCategory |
| **Order** | 8080 | order_db (PG 5434) | Orders & shopping cart | Order, OrderItem, ShoppingCart |
| **Payment** | 8080 | shared (PG 5432) | Payment processing | Payment, Transaction |
| **Notification** | 8080 | none | User notifications | (stateless) |

---

## 🔐 Identity Service

**Location**: `/microservice-identity`

### Purpose

Handles all authentication and authorization:
- User registration and login
- JWT token generation and validation
- Role-based access control (RBAC)
- User profile management
- Token refresh

### Database Schema

```
UserCredentials
├── id: UUID (PK)
├── email: String (UNIQUE)
├── password: String (hashed)
├── role: Enum (ADMIN, CUSTOMER, SELLER)
├── isActive: Boolean
├── createdAt: Instant
└── updatedAt: Instant

RefreshToken
├── id: UUID (PK)
├── userId: UUID (FK → UserCredentials)
├── token: String (UNIQUE)
├── expiresAt: Instant
└── createdAt: Instant

UserProfile
├── id: UUID (PK)
├── userId: UUID (FK → UserCredentials)
├── firstName: String
├── lastName: String
├── profileImageUrl: String
├── bio: String
├── createdAt: Instant
└── updatedAt: Instant
```

### Key APIs

```
POST   /api/v1/auth/register          Register new user
POST   /api/v1/auth/login             Login (returns access + refresh tokens)
POST   /api/v1/auth/refresh           Refresh access token
GET    /.well-known/jwks.json         Public JWK Set (for token validation)

GET    /api/v1/profile                Get current user profile
PUT    /api/v1/profile                Update current user profile
GET    /api/v1/profile/{userId}       Get user profile by ID

POST   /api/v1/admin/users            Create user (admin only)
PUT    /api/v1/admin/users/{userId}   Update user role (admin only)
```

### Key Classes

```
domain/entity/
  ├── UserCredentials.java       # Main entity
  ├── UserRole.java              # Enum: ADMIN, CUSTOMER, SELLER
  └── RefreshToken.java

application/service/
  ├── AuthService.java           # Login, register, refresh
  └── JwtTokenProvider.java      # Token generation/validation

infra/web/
  ├── AuthController.java        # /api/v1/auth endpoints
  ├── AdminController.java       # Admin-only endpoints
  ├── WellKnownController.java   # /.well-known/jwks.json
  └── UserCredentialsResponse.java

common/security/
  └── JwtTokenProvider.java      # Shared JWT utility
```

### How to Use

**Validate JWT in Your Service:**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwkSetUri("http://identity:8080/.well-known/jwks.json")
                )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}
```

**Get Current User in Endpoint:**

```java
@GetMapping("/api/v1/profile")
public ResponseEntity<ProfileDTO> getProfile(
    @AuthenticationPrincipal JwtAuthenticationToken token) {
    UUID userId = UUID.fromString(token.getName());
    // ... get and return profile
}
```

**Publish User Created Event:**

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

// Consumed by Notification Service to send welcome email
```

---

## 📦 Product Service

**Location**: `/microservice-product`

### Purpose

Complete product catalog management:
- Product browsing and search
- Category management
- Inventory/stock level tracking
- Product variants (SKUs)
- Wishlist management

### Database Schema

```
Product
├── id: UUID (PK)
├── name: String
├── description: String
├── categoryId: UUID (FK → ProductCategory)
├── createdAt: Instant
└── updatedAt: Instant

ProductCategory
├── id: UUID (PK)
├── name: String
├── description: String
├── isActive: Boolean
├── createdAt: Instant
└── updatedAt: Instant

ProductSKU (Stock Keeping Unit - variants)
├── id: UUID (PK)
├── productId: UUID (FK → Product)
├── sku: String (e.g., "PROD-001-RED-M")
├── price: BigDecimal
├── stockLevel: Integer
├── weight: Double
├── dimensions: String
├── isActive: Boolean
├── createdAt: Instant
└── updatedAt: Instant

Wishlist
├── id: UUID (PK)
├── userId: UUID (external - from Identity Service)
├── productId: UUID (FK → Product)
├── addedAt: Instant
└── updatedAt: Instant
```

### Key APIs

**Catalog (Public):**

```
GET    /api/v1/catalogue/products                List products (paginated)
GET    /api/v1/catalogue/products/{id}          Get product details
GET    /api/v1/catalogue/products/search?q=...  Search products
GET    /api/v1/catalogue/categories             List categories
GET    /api/v1/catalogue/wishlist               Get user's wishlist
POST   /api/v1/catalogue/wishlist               Add to wishlist
DELETE /api/v1/catalogue/wishlist/{productId}   Remove from wishlist
```

**Admin:**

```
POST   /api/v1/admin/products                  Create product
PUT    /api/v1/admin/products/{id}             Update product
PUT    /api/v1/admin/products/{id}/stock       Update stock level
DELETE /api/v1/admin/products/{id}             Soft delete product

POST   /api/v1/admin/categories                Create category
PUT    /api/v1/admin/categories/{id}           Update category
```

### Key Classes

```
domain/entity/
  ├── Product.java              # Main aggregate root
  ├── ProductSKU.java           # Variant
  ├── ProductCategory.java      # Category
  └── Wishlist.java

application/service/
  ├── CatalogueService.java     # Search, listing
  ├── InventoryService.java     # Stock management
  ├── WishlistService.java      # Wishlist operations
  └── AdminProductService.java

infra/web/
  ├── CatalogueController.java
  └── AdminProductController.java
```

### How to Use

**Check Product Availability (from Order Service):**

```java
@Service
public class OrderService {
    private final RestTemplate restTemplate;
    
    public void createOrder(CreateOrderRequest request) {
        // Call Product Service
        String url = "http://product:8080/api/v1/catalogue/products/{id}";
        ProductDTO product = restTemplate.getForObject(url, ProductDTO.class, request.productId());
        
        if (product.stockLevel() < request.quantity()) {
            throw new InsufficientStockException();
        }
    }
}
```

**Reduce Stock on Order (future - via events):**

```java
@Component
public class ProductInventoryListener {
    @RabbitListener(queues = "order.created")
    public void onOrderCreated(OrderCreatedEvent event) {
        for (OrderItem item : event.getItems()) {
            inventoryService.reduceStock(item.getProductId(), item.getQuantity());
        }
    }
}
```

**Stock Change Events:**

```java
@Getter
@EqualsAndHashCode(callSuper = false)
public class ProductStockChangedEvent extends ApplicationEvent {
    private final UUID productId;
    private final int quantity;
    private final String reason; // "order.created", "refund", "admin"
    
    public ProductStockChangedEvent(Object source, UUID productId, int quantity, String reason) {
        super(source);
        this.productId = productId;
        this.quantity = quantity;
        this.reason = reason;
    }
}
```

---

## 🛒 Order Service

**Location**: `/microservice-order`

### Purpose

Order lifecycle management:
- Shopping cart management
- Order creation and processing
- Order status tracking
- Order history

### Database Schema

```
ShoppingCart
├── id: UUID (PK)
├── userId: UUID (external - from Identity Service)
├── createdAt: Instant
└── updatedAt: Instant

CartItem
├── id: UUID (PK)
├── cartId: UUID (FK → ShoppingCart)
├── productId: UUID (external - from Product Service)
├── skuId: UUID (external - from Product Service)
├── quantity: Integer
├── unitPrice: BigDecimal
├── addedAt: Instant
└── updatedAt: Instant

Order
├── id: UUID (PK)
├── userId: UUID (external - from Identity Service)
├── status: Enum (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
├── totalAmount: BigDecimal
├── shippingAddress: String
├── billingAddress: String
├── createdAt: Instant
├── updatedAt: Instant
└── deliveredAt: Instant (nullable)

OrderItem
├── id: UUID (PK)
├── orderId: UUID (FK → Order)
├── productId: UUID (external - from Product Service)
├── skuId: UUID (external - from Product Service)
├── quantity: Integer
├── unitPrice: BigDecimal
└── subtotal: BigDecimal
```

### Key APIs

```
# Shopping Cart
POST   /api/v1/cart/items              Add item to cart
GET    /api/v1/cart                    Get cart
DELETE /api/v1/cart/items/{itemId}     Remove item from cart
PUT    /api/v1/cart/items/{itemId}     Update item quantity

# Orders
POST   /api/v1/orders                  Create order from cart
GET    /api/v1/orders                  List user's orders
GET    /api/v1/orders/{orderId}        Get order details
PUT    /api/v1/orders/{orderId}/status Update order status (admin)
```

### Key Classes

```
domain/entity/
  ├── Order.java                 # Aggregate root
  ├── OrderItem.java
  ├── ShoppingCart.java
  └── OrderStatus.java           # Enum

application/service/
  ├── OrderService.java          # Create, list orders
  ├── CartService.java           # Cart operations
  └── OrderStatusService.java    # Status updates

infra/web/
  ├── OrderController.java
  └── CartController.java
```

### How to Use

**Creating an Order:**

1. User adds items to cart (via CartService)
2. User initiates checkout (POST /orders)
3. Order Service:
   - Validates cart items still available (calls Product Service)
   - Creates order
   - Publishes `OrderCreatedEvent`
4. Payment Service receives event and processes payment
5. Notification Service receives event and sends confirmation email

**Event Flow:**

```java
// Order Service publishes
eventPublisher.publishEvent(new OrderCreatedEvent(
    this,
    order.getId(),
    order.getUserId(),
    order.getItems()
));

// Notification Service consumes
@RabbitListener(queues = "order.created")
public void sendOrderConfirmation(OrderCreatedEvent event) {
    emailService.sendOrderConfirmation(event.getUserId(), event.getOrderId());
}
```

---

## 💳 Payment Service

**Location**: `/microservice-payment`

### Purpose

Payment processing and transaction management:
- Payment method registration
- Transaction processing
- Refund handling
- Payment status tracking

### Database Schema

```
Payment
├── id: UUID (PK)
├── orderId: UUID (external - from Order Service)
├── userId: UUID (external - from Identity Service)
├── amount: BigDecimal
├── currency: String
├── status: Enum (PENDING, PROCESSING, SUCCESS, FAILED, REFUNDED)
├── paymentMethodId: UUID (FK → PaymentMethod)
├── transactionId: String (external payment processor ID)
├── createdAt: Instant
├── updatedAt: Instant
└── completedAt: Instant (nullable)

PaymentMethod
├── id: UUID (PK)
├── userId: UUID (external - from Identity Service)
├── type: Enum (CREDIT_CARD, DEBIT_CARD, WALLET)
├── lastFourDigits: String
├── expiryMonth: Integer
├── expiryYear: Integer
├── isDefault: Boolean
├── isActive: Boolean
├── createdAt: Instant
└── updatedAt: Instant
```

### Key APIs

```
# Payment Methods
POST   /api/v1/payment-methods              Register payment method
GET    /api/v1/payment-methods             List user's payment methods
DELETE /api/v1/payment-methods/{methodId}  Delete payment method

# Payments
POST   /api/v1/payments                    Process payment
GET    /api/v1/payments/{paymentId}        Get payment details
POST   /api/v1/payments/{paymentId}/refund Request refund
```

### Key Classes

```
domain/entity/
  ├── Payment.java               # Aggregate root
  ├── PaymentMethod.java
  └── PaymentStatus.java         # Enum

application/service/
  ├── PaymentService.java        # Process payments
  ├── RefundService.java         # Handle refunds
  └── PaymentMethodService.java  # Payment method management

infra/web/
  └── PaymentController.java

infra/external/
  └── PaymentProviderClient.java # (e.g., Stripe, PayPal)
```

### How to Use

**Processing Payment (triggered by Order Service):**

```java
@Component
public class PaymentProcessor {
    @RabbitListener(queues = "order.created")
    public void processOrderPayment(OrderCreatedEvent event) {
        // 1. Fetch order details
        // 2. Create payment record
        // 3. Call payment provider
        // 4. Publish PaymentProcessedEvent or PaymentFailedEvent
        
        paymentService.processPayment(event.getOrderId());
    }
}
```

**Event Publishing:**

```java
@Getter
@EqualsAndHashCode(callSuper = false)
public class PaymentProcessedEvent extends ApplicationEvent {
    private final UUID paymentId;
    private final UUID orderId;
    private final BigDecimal amount;
    private final boolean success;
    
    // Constructor...
}
```

---

## 🔔 Notification Service

**Location**: `/microservice-notification`

### Purpose

Send notifications to users:
- Email notifications (welcome, order confirmation, order shipped, etc)
- In-app notifications (future)
- SMS notifications (future)
- Notification preferences

### Database Schema

```
NotificationPreference
├── id: UUID (PK)
├── userId: UUID (external - from Identity Service)
├── emailOnOrderCreated: Boolean (default: true)
├── emailOnOrderShipped: Boolean (default: true)
├── emailOnPaymentFailed: Boolean (default: true)
├── emailWeeklyNewsletter: Boolean (default: false)
├── updatedAt: Instant
└── createdAt: Instant

NotificationLog (optional - for audit)
├── id: UUID (PK)
├── userId: UUID
├── type: String (e.g., "order_confirmation", "welcome")
├── sentAt: Instant
└── status: String (sent, failed)
```

### Key APIs

```
GET    /api/v1/preferences               Get notification preferences
PUT    /api/v1/preferences               Update preferences
```

### Key Classes

```
application/service/
  ├── EmailNotificationService.java
  ├── NotificationPreferenceService.java
  └── EventListeners/
      ├── ProfileEventListener.java      # Listens to ProfileCreatedEvent
      ├── OrderEventListener.java        # Listens to OrderCreatedEvent
      └── PaymentEventListener.java      # Listens to PaymentProcessedEvent

infra/external/
  └── EmailProvider.java                 # (e.g., SendGrid, AWS SES)

infra/web/
  └── NotificationController.java
```

### How to Use

**Listening to Events:**

```java
@Component
@Slf4j
public class ProfileEventListener {
    private final EmailNotificationService emailService;
    
    @RabbitListener(queues = "profile.created")
    public void onProfileCreated(ProfileCreatedEvent event) {
        try {
            log.info("Sending welcome email to: {}", event.getEmail());
            emailService.sendWelcomeEmail(event.getEmail(), event.getUserId());
        } catch (Exception e) {
            log.error("Failed to send welcome email", e);
            // Could implement retry logic here
        }
    }
}

@Component
@Slf4j
public class OrderEventListener {
    private final EmailNotificationService emailService;
    private final NotificationPreferenceService preferenceService;
    
    @RabbitListener(queues = "order.created")
    public void onOrderCreated(OrderCreatedEvent event) {
        // Check user preferences
        if (preferenceService.shouldSendOrderConfirmation(event.getUserId())) {
            emailService.sendOrderConfirmation(
                event.getUserId(),
                event.getOrderId()
            );
        }
    }
}
```

---

## 🔄 Inter-Service Communication

### HTTP Calls (Synchronous)

**From Order Service to Product Service:**

```java
@Service
public class OrderService {
    private final RestTemplate restTemplate;
    
    public void createOrder(CreateOrderRequest request) {
        // Validate product availability
        try {
            ProductDTO product = restTemplate.getForObject(
                "http://product:8080/api/v1/catalogue/products/{id}",
                ProductDTO.class,
                request.productId()
            );
            
            if (product.stockLevel() < request.quantity()) {
                throw new InsufficientStockException();
            }
        } catch (RestClientException e) {
            log.error("Product service unavailable", e);
            throw new ServiceUnavailableException("Product service");
        }
    }
}
```

### Event Publishing (Asynchronous - RabbitMQ)

**From Order Service:**

```java
@Service
public class OrderService {
    private final ApplicationEventPublisher eventPublisher;
    private final RabbitTemplate rabbitTemplate;
    
    @Transactional
    public UUID createOrder(CreateOrderRequest request) {
        Order order = new Order(...);
        orderRepository.save(order);
        
        // Publish to local listeners
        eventPublisher.publishEvent(new OrderCreatedEvent(this, order));
        
        // Also publish to RabbitMQ for cross-service
        rabbitTemplate.convertAndSend(
            "order.exchange",
            "order.created",
            new OrderCreatedEvent(this, order)
        );
        
        return order.getId();
    }
}
```

**Configuration:**

```java
@Configuration
public class RabbitMQConfig {
    
    // Order Events
    @Bean
    public Exchange orderExchange() {
        return new TopicExchange("order.exchange", true, false);
    }
    
    @Bean
    public Queue orderCreatedQueue() {
        return new Queue("order.created", true);
    }
    
    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder
            .bind(orderCreatedQueue())
            .to(orderExchange())
            .with("order.created");
    }
}
```

---

## 🚀 Working with Microservices

### Modifying an Existing Service

1. Navigate to `/microservice-{name}`
2. Update code in `domain/`, `application/`, or `infra/` as needed
3. Test locally
4. Commit and push

### Adding New Endpoint to Service

```java
// 1. Create DTO in application/dto/
public record CreateProductRequest(
    @NotBlank String name,
    @NotNull UUID categoryId,
    @NotNull BigDecimal price
) {}

// 2. Create service method in application/service/
@Service
public class ProductService {
    public UUID createProduct(CreateProductRequest request) {
        // Business logic
    }
}

// 3. Create controller in infra/web/
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    @PostMapping
    public ResponseEntity<StandardResponse<UUID>> create(
        @Valid @RequestBody CreateProductRequest request) {
        UUID id = service.createProduct(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(StandardResponse.success(id));
    }
}
```

### Publishing New Events

1. Define event class in `application/event/`
2. Publish via `ApplicationEventPublisher` in service
3. Create listener in consuming service (or set up RabbitMQ queue)
4. Test event flow

---

## 📊 Service Dependencies Matrix

```
                    Identity  Product  Order  Payment  Notification
Identity Service       -        calls    calls  calls      listens
Product Service        -         -       calls   -          listens
Order Service        validates  calls    -      calls      publishes
Payment Service      validates   -        -      -          publishes
Notification Svc       -         -        -      -          (consumer)
```

**Key**: 
- `calls`: Makes HTTP calls to
- `validates`: Validates JWT from
- `listens`: Listens to events from
- `publishes`: Publishes events to

---

**Status**: 🚧 In Development | **Last Updated**: April 2026
