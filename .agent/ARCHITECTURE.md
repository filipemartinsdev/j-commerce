# Architecture Patterns

## Layered Architecture

```
src/main/java/com/{module}/
├── application/    # Use cases, DTOs, exceptions, events
├── domain/         # Entities, value objects, domain logic
├── infra/          # Controllers, repositories, config
└── config/         # Bean configurations
```

## Application Layer

- **dto/**: Request/Response records, PagedResponse<T>
- **service/**: Business logic, orchestration
- **event/**: Domain events for async processing
- **exception/**: Custom runtime exceptions

## Domain Layer

- **entity/**: JPA entities with @Id, @GeneratedValue, relationships
- Business logic in entity methods when simple
- Complex logic in domain services

## Infra Layer

- **persistence/**: JpaRepository interfaces
- **web/**: @RestController, @RequestMapping

## Dependency Rule

Dependencies point inward: Application -> Domain -> Infra

## Entity Patterns

### Basic Entity
```java
@Entity @Table(name = "table_name")
@Data @NoArgsConstructor @AllArgsConstructor
public class Entity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
```

### Soft Delete
```java
@Column(name = "is_active")
private Boolean isActive = true;
// Never delete: entity.setIsActive(false);
```

### ManyToMany with JoinTable
```java
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "junction_table",
    joinColumns = @JoinColumn(name = "source_id"),
    inverseJoinColumns = @JoinColumn(name = "target_id")
)
private List<RelatedEntity> relatedEntities = new ArrayList<>();
```

### Enum in Entity
```java
public enum Value {
    VALUE1(1, "VALUE1"),
    VALUE2(2, "VALUE2");

    private final int id;
    private final String name;

    Value(int id, String name) { ... }

    public static Value fromId(int id) { ... }
}
```

## Repository Pattern

```java
public interface EntityRepository extends JpaRepository<Entity, UUID> {
    Optional<Entity> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<Entity> findAll(Pageable pageable);
}
```

## Service Pattern

```java
@Service
public class EntityService {
    private final EntityRepository repository;
    private final EntityMapper mapper;

    public EntityService(EntityRepository repository, EntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public Response create(Request request) {
        Entity entity = new Entity();
        entity.setField(request.field());
        return mapper.toResponse(repository.save(entity));
    }

    public Response getById(UUID id) {
        return mapper.toResponse(
            repository.findById(id)
                .orElseThrow(() -> new NotFoundException("message"))
        );
    }
}
```

## Controller Pattern

```java
@RestController
@RequestMapping("/api/v1")
public class EntityController {
    private final EntityService service;

    @PostMapping
    public ResponseEntity<Response> create(@Valid @RequestBody Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }
}
```

## Pagination Pattern

```java
public PagedResponse<EntityResponse> getAll(Pageable pageable) {
    Page<Entity> page = repository.findAll(pageable);
    return PagedResponse.<EntityResponse>builder()
        .page(page.getNumber())
        .size(page.getSize())
        .isLast(page.isLast())
        .totalPages(page.getTotalPages())
        .totalElements(page.getTotalElements())
        .content(page.getContent().stream()
            .map(mapper::toResponse)
            .toList())
        .build();
}
```

## Event Publishing Pattern

```java
@Service
public class EntityService {
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void create(Request request) {
        Entity entity = repository.save(newEntity);
        eventPublisher.publishEvent(new DomainEvent(entity));
    }
}
```

## Exception Pattern

```java
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
```

## Validation Pattern

- Use Jakarta Validation (@NotNull, @NotBlank, @Email, etc.)
- @Valid on nested objects in DTOs
- GlobalExceptionHandler for mapping to HTTP responses

## DDD Basic Concepts

### Aggregate
Single entity as root with related entities accessed only through it.

### Value Object
Immutable object defined by attributes, not identity.

### Domain Event
Something meaningful happened in the domain.

## Code Quality Rules

1. **Single Responsibility**: One class, one responsibility
2. **Small Methods**: Extract private methods > 10 lines
3. **No Comments**: Code explains itself; use @DisplayName for tests
4. **DRY**: Don't Repeat Yourself
5. **Law of Demeter**: Limit chain calls (entity.getX().getY())
6. **Final Fields**: Use final for dependencies

## Refactoring Triggers

- Method > 20 lines -> Extract
- Duplicate code -> Extract to method
- Primitive obsession -> Create Value Object
- Feature envy -> Move method to entity
- God class -> Split into smaller services