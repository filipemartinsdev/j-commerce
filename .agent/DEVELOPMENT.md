# Development Standards

## Code Style

### File Structure

```
src/main/java/com/{module}/{layer}/
```

### Imports Order

1. java.*
2. jakarta.*
3. org.springframework.*
4. com.{project}.*
5. other packages

### Naming Conventions

- **Classes**: PascalCase (EntityService)
- **Methods**: camelCase (getById)
- **Variables**: camelCase (userId)
- **Constants**: UPPER_SNAKE_CASE
- **Packages**: lowercase (entity, dto)

### Lombok Usage

```java
@Entity @Table(name = "table")
@Data @NoArgsConstructor @AllArgsConstructor
public class Entity { }
```

Avoid @Setter on entities with complex validation; use builders or factory methods.

## DTO Patterns

### Request DTO

```java
public record CreateEntityRequest(
    @NotBlank String name,
    @NotNull Integer categoryId,
    @Size(max = 500) String description
) {}
```

### Response DTO

```java
public record EntityResponse(
    UUID id,
    String name,
    Instant createdAt
) {}
```

### Enum in DTO

```java
public record EntityResponse(
    UUID id,
    Role.Value role
) {}
```

## Database Patterns

### Migrations (Flyway)

```
src/main/resources/db/migration/
├── V1__init.sql
├── V2__alter_table.sql
└── V3__populate_data.sql
```

### Migration Structure

```sql
CREATE TABLE entity (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);
```

### Soft Delete Convention

- Never DELETE FROM table
- UPDATE table SET is_active = false WHERE id = ?

## API Documentation (Swagger OpenAPI)

### Interface-Based Pattern

Controllers MUST NOT contain Swagger annotations. Create a `*ControllerDocs` interface:

```java
package com.{module}.{submodule}.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "{Feature Name}")
public interface {ControllerName}Docs {
    @Operation(summary = "{Action description}")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "{Action} successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "{Resource} not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Client not authenticated",
                    content = @Content
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<StandardResponse<{ResponseDto}>> {methodName}({params});
}
```

### Controller Implementation

Controllers implementing docs interfaces do NOT use `@Override`:

```java
@RestController
@RequestMapping("/api/v1/{resource}")
public class {ControllerName}Controller implements {ControllerName}Docs {

    @PostMapping
    public ResponseEntity<StandardResponse<{ResponseDto}>> create(@Valid @RequestBody {RequestDto} request) {
        // implementation
    }
}
```

### OpenAPIConfig Required

```java
@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("{Service Name}").version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
```

### SecurityConfig - Permit Swagger Paths

```java
.requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
```

### Documentation Review Checklist

- [ ] `*ControllerDocs` interface created in `docs/` package
- [ ] Controller implements the docs interface
- [ ] NO Swagger annotations on controller class
- [ ] `@Tag` on docs interface, `@Operation` on every method
- [ ] `@ApiResponses` covers ALL exceptions thrown by service
- [ ] `@SecurityRequirement(name = "bearerAuth")` on protected endpoints only
- [ ] GlobalExceptionHandler mappings match documented response codes
- [ ] OpenAPIConfig exists with correct service title

## API Design

### URL Patterns

- `/api/v1/resource` - collection
- `/api/v1/resource/{id}` - single resource
- `/admin/api/v1/resource` - admin operations
- Query params: `?page=0&size=10&sort=field,desc`

### HTTP Status Codes

- 200 OK - successful GET, PUT, PATCH
- 201 CREATED - successful POST
- 204 NO CONTENT - successful DELETE (no response body)
- 400 BAD REQUEST - validation error
- 401 UNAUTHORIZED - missing/invalid token
- 403 FORBIDDEN - insufficient permissions
- 404 NOT FOUND - resource not exists
- 500 INTERNAL SERVER ERROR - unexpected error

### Response Wrapper

```java
public record StandardResponse<T>(
    String status,
    T data,
    String message
) {
    public static <T> StandardResponse<T> success(T data) {
        return new StandardResponse<>("success", data, null);
    }
}

public record PagedResponse<T>(
    int page,
    int size,
    boolean isLast,
    int totalPages,
    long totalElements,
    List<T> content
) {}
```

## Security

### JWT Claims

```
sub: userId (UUID)
scope: roles (space-separated)
token_type: access | refresh
iss: issuer
iat: issued at
exp: expiration
jti: JWT ID (for refresh tokens)
```

### Role Scope Mapping

- ADMIN role -> SCOPE_ADMIN
- USER role -> SCOPE_USER
- STOCK_MANAGER role -> SCOPE_STOCK_MANAGER

### Protected Endpoint

```java
@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
@DeleteMapping("/{id}")
public void delete(UUID id) { }
```

## Configuration

### application.yml

```yaml
spring:
  application:
    name: microservice-{module}
  datasource:
    url: jdbc:postgresql://host:5432/db
    username: user
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
```

### Bean Configuration

```java
@Configuration
public class BeanConfig {
    @Bean
    public EntityRepository repository(EntityManager em) {
        return new JpaRepositoryImplementation<>(Entity.class, em);
    }
}
```

## Event-Driven Patterns

### Publishing Event

```java
@Service
public class EntityService {
    private final ApplicationEventPublisher publisher;

    @Transactional
    public void create(Request request) {
        Entity entity = repository.save(request.toEntity());
        publisher.publishEvent(new EntityCreatedEvent(entity));
    }
}
```

### Consuming Event (Sync)

```java
@Component
public class EntityEventListener {
    @EventListener
    public void onEntityCreated(EntityCreatedEvent event) {
        // handle event
    }
}
```

## Testing Patterns

All unit tests MUST follow the pattern in TESTING.md.

## Build & Run

```bash
./mvnw spring-boot:run
./mvnw test
./mvnw package -DskipTests
```

## Docker

```bash
docker build -t microservice-{module} .
docker run -p 8080:8080 microservice-{module}
```

## Code Review Checklist

- [ ] No TODOs left in code
- [ ] No System.out.println
- [ ] All exceptions are handled
- [ ] @Transactional used correctly
- [ ] Hardcoded values in config
- [ ] No magic numbers
- [ ] Meaningful variable names
- [ ] Methods are small (<20 lines)
- [ ] Tests cover success AND failure paths