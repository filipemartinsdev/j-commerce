# API Documentation Skill - Swagger OpenAPI

## Overview

This skill defines the exact pattern for documenting APIs using Springdoc OpenAPI (v3.x). All microservices MUST follow this documentation standard to ensure consistency across the platform.

## Core Principle: Interface-Based Documentation

Controllers MUST NOT contain Swagger annotations directly. Instead, create a separate `*ControllerDocs` interface that holds all OpenAPI annotations, and have the controller implement it. This keeps business logic clean and separates documentation from implementation.

### Directory Structure

```
src/main/java/com/{module}/
├── docs/                          # NEW: Documentation interfaces
│   ├── AuthControllerDocs.java
│   ├── AdminControllerDocs.java
│   └── UserControllerDocs.java
├── infra/web/                     # Controllers implement docs interfaces
│   ├── AuthController.java        # implements AuthControllerDocs
│   ├── AdminController.java       # implements AdminControllerDocs
│   └── UserController.java        # implements UserControllerDocs
```

## Required Dependency

Add to every microservice `pom.xml`:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.0.2</version>
</dependency>
```

Swagger UI is available at: `/swagger-ui/index.html`
OpenAPI JSON is available at: `/v3/api-docs`

## OpenAPI Configuration

Every microservice MUST have an `OpenAPIConfig` class:

```java
package com.{module}.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
    private final String TITLE = "{Service Name}";
    private final String VERSION = "1.0.0";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(TITLE)
                        .version(VERSION)
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }
}
```

### Security Config - Permit Swagger Paths

In `SecurityConfig.java`, allow unauthenticated access to Swagger endpoints:

```java
.requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
```

## Documentation Interface Pattern

### Template

```java
package com.{module}.{submodule}.docs;

import com.{module}.common.dto.StandardResponse;
import com.{module}.common.dto.PagedResponse;
import com.{module}.{submodule}.application.dto.{RequestDto};
import com.{module}.{submodule}.application.dto.{ResponseDto};
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "{Tag Name}")
public interface {ControllerName}Docs {

    @Operation(summary = "{Brief action description}")
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
                    responseCode = "400",
                    description = "{Validation error description}",
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
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authenticated user can't perform this operation",
                    content = @Content
            )
    })
    ResponseEntity<StandardResponse<{ResponseDto}>> {methodName}({params});
}
```

### Protected Endpoints

Add `@SecurityRequirement(name = "bearerAuth")` to every endpoint that requires authentication:

```java
@SecurityRequirement(name = "bearerAuth")
@Operation(summary = "Get user by ID")
@ApiResponses({ ... })
ResponseEntity<StandardResponse<UserResponse>> getUserById(UUID userId);
```

### Public Endpoints

Do NOT add `@SecurityRequirement` to public endpoints:

```java
@Operation(summary = "Register new user")
@ApiResponses({ ... })
ResponseEntity<Void> register(RegisterRequest request);
```

## Allowed Swagger Annotations

Only use these annotations from `io.swagger.v3.oas.annotations`:

| Annotation | Scope | Purpose |
|---|---|---|
| `@Tag` | Interface | Group endpoints by feature (e.g., "Authentication", "Admin management") |
| `@Operation` | Method | Describe endpoint with `summary` |
| `@ApiResponses` | Method | Group multiple `@ApiResponse` annotations |
| `@ApiResponse` | Method | Document a single HTTP response with `responseCode`, `description`, `content` |
| `@Schema` | Inside @Content | Define response type via `implementation = ClassName.class` |
| `@Content` | Inside @ApiResponse | Specify `mediaType` and `schema` |
| `@SecurityRequirement` | Method | Mark endpoint as requiring authentication (`name = "bearerAuth"`) |

### Annotations NOT to Use

Do NOT use these annotations unless explicitly requested:
- `@Parameter` - No explicit path/query parameter documentation
- `@RequestBody` - No explicit request body documentation
- `@Hidden` - No hidden endpoints
- `@ExternalDocumentation` - No external doc links
- `@Callback` - No callback definitions

## Response Documentation Rules

### Success Responses (2xx)

For endpoints that return data, document the full response structure:

```java
@ApiResponse(
        responseCode = "200",
        description = "{Action} successfully",
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = StandardResponse.class)
        )
)
```

For endpoints that return no body (201 CREATED with void):

```java
@ApiResponse(
        responseCode = "201",
        description = "{Resource} created successfully",
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = StandardResponse.class)
        )
)
```

### Error Responses (4xx, 5xx)

Error responses that return `StandardResponse` with a message:

```java
@ApiResponse(
        responseCode = "404",
        description = "{Resource} not found",
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = StandardResponse.class)
        )
)
```

Error responses with no body content (401, 403 from Spring Security):

```java
@ApiResponse(
        responseCode = "401",
        description = "Client not authenticated",
        content = @Content
)
```

### Paginated Responses

For endpoints returning `PagedResponse<T>`:

```java
@ApiResponse(
        responseCode = "200",
        description = "{Resources} retrieved successfully",
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = StandardResponse.class)
        )
)
```

## Standard HTTP Status Codes

Map these codes to `@ApiResponse` annotations:

| Code | When to Document | Description Pattern |
|---|---|---|
| 200 | GET, PATCH success | "{Resource} retrieved/updated successfully" |
| 201 | POST success | "New {resource} registered/created successfully" |
| 400 | Validation errors, business rule violations | "{Specific error description}" |
| 401 | Protected endpoints (Spring Security) | "Client not authenticated" |
| 403 | Insufficient permissions | "Authenticated user can't perform this operation" |
| 404 | Resource not found exceptions | "{Resource} not found" |
| 500 | Catch-all (do NOT document, handled globally) | Not documented per endpoint |

## Exception-to-Response Mapping Checklist

When documenting a controller, review the `GlobalExceptionHandler` to ensure ALL exception responses are documented:

### Required Review Steps

1. **Read the controller** - List all endpoints and their operations
2. **Read the service layer** - Identify which exceptions each method can throw
3. **Read GlobalExceptionHandler** - Map each exception to its HTTP status code
4. **Cross-reference** - Ensure every thrown exception has a matching `@ApiResponse`

### Common Exception Mappings

| Exception | HTTP Status | @ApiResponse Description |
|---|---|---|
| `*NotFoundException` | 404 | "{Resource} not found" |
| `*AlreadyExistsException` | 400 | "{Resource} already exists" |
| `ForbiddenOperationException` | 403 | "Authenticated user can't perform this operation" |
| `BadJwtException` | 400 | "Invalid JWT token" |
| `MethodArgumentNotValidException` | 400 | Validation errors (Spring auto-handled) |
| `Exception` (catch-all) | 500 | Do NOT document per endpoint |

## Tag Naming Conventions

Use descriptive, user-facing tag names:

| Tag Name | Module |
|---|---|
| `"Authentication"` | Auth endpoints (login, register, refresh) |
| `"Admin management"` | Admin CRUD operations |
| `"User profile"` | User self-service endpoints |
| `"Well Known"` | Public JWKS endpoint |
| `"{Resource} management"` | Admin resource operations |
| `"{Resource} catalogue"` | Public read-only resource operations |

## Controller Implementation Pattern

The controller implements the docs interface with NO additional Swagger annotations and NO `@Override`:

```java
package com.{module}.{submodule}.infra.web;

import com.{module}.{submodule}.docs.{ControllerName}Docs;
import com.{module}.{submodule}.application.service.{ServiceName};
import com.{module}.{submodule}.application.dto.{RequestDto};
import com.{module}.{submodule}.application.dto.{ResponseDto};
import com.{module}.common.dto.StandardResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/{resource}")
public class {ControllerName}Controller implements {ControllerName}Docs {

    private final {ServiceName} service;

    public {ControllerName}Controller({ServiceName} service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<StandardResponse<{ResponseDto}>> create(@Valid @RequestBody {RequestDto} request) {
        {ResponseDto} response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(StandardResponse.success(response));
    }
}
```

## Documentation Review Checklist

Every time you create or modify a controller, verify:

- [ ] Created `*ControllerDocs` interface in `docs/` package
- [ ] Controller implements the docs interface
- [ ] NO Swagger annotations on the controller class itself
- [ ] `@Tag` on the docs interface with descriptive name
- [ ] `@Operation` with clear `summary` on every endpoint method
- [ ] `@ApiResponses` covers ALL possible responses for each endpoint
- [ ] `@SecurityRequirement(name = "bearerAuth")` on protected endpoints only
- [ ] All exception types thrown by the service are documented as `@ApiResponse`
- [ ] Success responses use `@Schema(implementation = StandardResponse.class)`
- [ ] Error responses without body use empty `@Content`
- [ ] Response codes match GlobalExceptionHandler mappings
- [ ] OpenAPIConfig exists with correct service title and bearerAuth scheme
- [ ] SecurityConfig permits `/v3/api-docs/**` and `/swagger-ui/**`

## Creating Documentation for a New Microservice

When adding OpenAPI docs to a new microservice:

1. **Add dependency** to `pom.xml`: `springdoc-openapi-starter-webmvc-ui` version `3.0.2`
2. **Create `OpenAPIConfig.java`** in config package with service title and bearerAuth
3. **Update `SecurityConfig.java`** to permit Swagger paths (`/v3/api-docs/**`, `/swagger-ui/**`)
4. **For each controller**:
   a. Create `docs/` package under the module
   b. Create `{ControllerName}Docs.java` interface
   c. Add `@Tag` with appropriate name
   d. Add method signatures matching controller methods
   e. Add `@Operation`, `@ApiResponses`, `@SecurityRequirement` annotations
   f. Make controller implement the docs interface (DO NOT add `@Override` annotations)
5. **Create `GlobalExceptionHandler`** if it doesn't exist (see section below)
6. **Review GlobalExceptionHandler** and document all exception responses in `@ApiResponse`
7. **Verify** by running the service and checking `/swagger-ui/index.html`

## GlobalExceptionHandler Requirement

Every microservice MUST have a `GlobalExceptionHandler` to properly map exceptions to HTTP responses.

### Required Location

```
src/main/java/com/{module}/application/handler/GlobalExceptionHandler.java
```

### Template

```java
package com.{module}.application.handler;

import com.{module}.application.dto.StandardResponse;
import com.{module}.application.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<Void>> handleException(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StandardResponse.error(e.getMessage()));
    }

    @ExceptionHandler({NotFoundException}.class)
    public ResponseEntity<StandardResponse<Void>> handleNotFound({NotFoundException} e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail(e.getMessage()));
    }

    @ExceptionHandler({BusinessException}.class)
    public ResponseEntity<StandardResponse<Void>> handleBusinessException({BusinessException} e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<StandardResponse<Void>> handleForbiddenOperation(ForbiddenOperationException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(StandardResponse.fail(e.getMessage()));
    }
}
```

### Standard HTTP Status Mappings

| Exception Pattern | HTTP Status | StandardResponse Method |
|---|---|---|
| `*NotFoundException` | 404 NOT_FOUND | `StandardResponse.fail(message)` |
| `*AlreadyExistsException` | 400 BAD_REQUEST | `StandardResponse.fail(message)` |
| `*Cant*Exception` | 400 BAD_REQUEST | `StandardResponse.fail(message)` |
| `ForbiddenOperationException` | 403 FORBIDDEN | `StandardResponse.fail(message)` |
| `BadJwtException` | 400 BAD_REQUEST | `StandardResponse.fail(message)` |
| `Exception` (catch-all) | 500 INTERNAL_SERVER_ERROR | `StandardResponse.error(message)` |

### GlobalExceptionHandler Review Checklist

When documenting controllers, verify:

- [ ] GlobalExceptionHandler exists in the microservice
- [ ] All exception classes from service layer have corresponding `@ExceptionHandler` methods
- [ ] HTTP status codes in handler match those documented in `@ApiResponse` annotations
- [ ] All documented exception responses in controller match actual handler behavior
- [ ] Logging is present for catch-all exception handler
