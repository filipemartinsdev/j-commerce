# Testing Pattern

## Strict Pattern for Integration Tests (Web MVC)

Integration tests for web controllers MUST follow this exact structure. No deviations allowed.

> **Note**: The package path `com.{module}.security.infra.web` and references to `AuthService` in this document are specific to the authentication microservice context. For other modules, adjust the package and service accordingly to match your domain.

### Test Class Template

```java
package com.{module}.{domain}.infra.web;

import tools.jackson.databind.ObjectMapper;
import com.identity.config.SecurityConfig;
import com.{module}.{domain}.application.dto.{DtoImport};
import com.{module}.{domain}.application.exception.*;
import com.{module}.{domain}.application.service.{ServiceName}Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({Controller}.class)
@Import(SecurityConfig.class)
public class {Controller}Tests {
    @MockitoBean private {ServiceName}Service {serviceName}Service;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
}
```

### Annotations Reference

| Annotation | Purpose |
|------------|---------|
| `@WebMvcTest(Controller.class)` | Tests only the web layer |
| `@Import(SecurityConfig.class)` | Import security configuration |
| `@MockitoBean` | Mock the service dependency |
| `@WithMockUser(authorities = "SCOPE_USER")` | Simulate authenticated user with role |
| `@ActiveProfiles("test")` | Use test profile when needed |

### Two Authentication Patterns for Controller Tests

Controllers can require authentication in two ways:
1. **SCOPE-only endpoints**: Only checks if the user has the required authority (SCOPE_ADMIN, SCOPE_USER, etc.)
2. **JWT-dependent endpoints**: Requires the JWT to extract user information (e.g., `@AuthenticationPrincipal Jwt jwt`)

Understanding which pattern to use is critical for correct test implementation.

### Pattern 1: SCOPE-Only Endpoints (No JWT Required)

When the controller does **NOT** use `@AuthenticationPrincipal Jwt jwt`, use ONLY `@WithMockUser`:

```java
import tools.jackson.databind.ObjectMapper;
import org.springframework.security.test.context.support.WithMockUser;

@WithMockUser(authorities = "SCOPE_ADMIN")
@Test @DisplayName("Should retrieve all prices and return status code 200")
void getAllPricesTestCase1() throws Exception {
    var priceResponse = new ProductSKUPriceResponse(UUID.randomUUID(), UUID.randomUUID(),
            "testing", BigDecimal.ONE, new PriceTypeResponse(1, "Base"),
            Instant.now(), null, Instant.now());

    var pagedResponse = PagedResponse.<ProductSKUPriceResponse>builder()
            .page(0).size(20).totalElements(1L).totalPages(1)
            .isLast(true).content(List.of(priceResponse)).build();

    when(productPriceService.getAllPrices(any())).thenReturn(pagedResponse);

    String expectedJSON = objectMapper.writeValueAsString(Map.of(
            "status", "success", "data", pagedResponse
    ));

    mockMvc.perform(get("/admin/api/v1/prices"))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJSON));
}
```

For multiple authorities:
```java
@WithMockUser(authorities = {"SCOPE_ADMIN", "SCOPE_STOCK_MANAGER"})
@Test @DisplayName("Should retrieve stock and return status code 200")
void getAllStockTestCase1() throws Exception {
    // ...
}
```

**Key Point**: Do NOT use `.with(jwt()...)` for SCOPE-only endpoints.

### Pattern 2: JWT-Dependent Endpoints (JWT Required)

When the controller uses `@AuthenticationPrincipal Jwt jwt` to extract user identity, you MUST use BOTH:
- `@WithMockUser` to pass authorization
- `.with(jwt()...)` to provide the JWT token with subject and authorities

```java
import tools.jackson.databind.ObjectMapper;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import java.util.Map;

@WithMockUser(authorities = "SCOPE_USER")
@Test @DisplayName("Should create wishlist item and return status code 201")
void createWishlistItemTestCase1() throws Exception {
    var request = new CreateWishlistItemRequest(productSKUId, 2);
    var response = new WishlistItemResponse(UUID.randomUUID(), productSKUId, "Product",
            new ProductPriceCatalogueResponse(BigDecimal.TEN, BigDecimal.TEN, 10, "DISCOUNT"));

    when(wishlistService.createItem(any())).thenReturn(response);

    String requestBody = objectMapper.writeValueAsString(request);
    String expectedJSON = objectMapper.writeValueAsString(Map.of(
            "status", "success", "data", response
    ));

    mockMvc.perform(post("/api/v1/wishlist")
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                    .authorities(List.of(new SimpleGrantedAuthority("SCOPE_USER"))))
    ).andExpect(status().isCreated())
      .andExpect(content().json(expectedJSON));
}
```

**Required Elements**:
| Element | Purpose |
|---------|---------|
| `.jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))` | Provides user ID for JWT extraction |
| `.authorities(List.of(new SimpleGrantedAuthority("SCOPE_ADMIN")))` | Provides authority for authorization |

**Why Both Are Needed**:
- `@WithMockUser` passes the security filter chain but doesn't provide JWT
- `.with(jwt()...)` provides the actual JWT token that the controller can extract
- `.authorities()` on jwt() ensures the OAuth2 resource server validates the token has proper scope

### Required Authentication Test Cases

For **every endpoint**, you MUST include both of these test cases:

#### JSON Generation with ObjectMapper

Always use `ObjectMapper` with `Map.of()` to generate expected JSON instead of hardcoded JSON strings:

```java
@Autowired private ObjectMapper objectMapper;

@Test @DisplayName("Should {behavior} and return status code 200")
void {methodName}TestCase1() throws Exception {
    var response = new {Response}(params);
    var pagedResponse = PagedResponse.<{Response}>builder()
            .page(0).size(20).totalElements(1L).totalPages(1)
            .isLast(true).content(List.of(response)).build();

    when({serviceName}Service.{method}(any())).thenReturn(pagedResponse);

    String expectedJSON = objectMapper.writeValueAsString(Map.of(
            "status", "success",
            "data", pagedResponse
    ));

    mockMvc.perform(get("/api/v1/{endpoint}"))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJSON));
}
```

**Key Points**:
- Import: `import tools.jackson.databind.ObjectMapper;`
- Use `Map.of("status", "success", "data", response)` structure
- Serialize with `objectMapper.writeValueAsString()`
- Do NOT use hardcoded JSON strings for expected responses

#### 401 Unauthorized (Not Authenticated)

```java
@Test @DisplayName("Should return status code 401 if client is not authenticated")
void {methodName}TestCase{N}() throws Exception {
    mockMvc.perform({httpMethod}("/api/v1/{endpoint}")
    ).andExpect(status().isUnauthorized());
}
```

**Important**: Do NOT add `@WithMockUser` or `.with(jwt()...)` for 401 tests.

#### 403 Forbidden (Wrong Role/Scope)

```java
@WithMockUser(authorities = "SCOPE_USER")
@Test @DisplayName("Should return status code 403 if client is not admin")
void {methodName}TestCase{N}() throws Exception {
    mockMvc.perform({httpMethod}("/api/v1/{endpoint}")
            .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                    .authorities(List.of(new SimpleGrantedAuthority("SCOPE_USER"))))
    ).andExpect(status().isForbidden());
}
```

**Note**: For SCOPE-only endpoints, you can omit `.with(jwt()...)`:
```java
@WithMockUser(authorities = "SCOPE_USER")
@Test @DisplayName("Should return status code 403 if client is not admin")
void {methodName}TestCase{N}() throws Exception {
    mockMvc.perform(get("/admin/api/v1/prices"))
            .andExpect(status().isForbidden());
}
```

#### 404 Not Found (Business Exception)

```java
@WithMockUser(authorities = "SCOPE_ADMIN")
@Test @DisplayName("Should return status code 404 if entity not found")
void {methodName}TestCase{N}() throws Exception {
    doThrow({NotFoundException}.class).when({serviceName}Service).{method}(any());

    mockMvc.perform(get("/api/v1/{endpoint}"))
            .andExpect(status().isNotFound());
}
```

### Decision Flowchart

```
Does controller use @AuthenticationPrincipal Jwt jwt?
├── NO (SCOPE-only) → Use @WithMockUser ONLY
│   ├── Success: @WithMockUser(authorities = "SCOPE_ADMIN")
│   ├── 401: No annotation, no jwt()
│   └── 403: @WithMockUser(authorities = "SCOPE_USER")
└── YES (JWT required) → Use BOTH @WithMockUser AND jwt()
    ├── Success: @WithMockUser + jwt() with subject + authorities
    ├── 401: No annotation, no jwt()
    └── 403: @WithMockUser + jwt() with wrong scope
```

### Complete Test Class Template (SCOPE-only)

```java
package com.{module}.infra.web.admin;

import tools.jackson.databind.ObjectMapper;
import com.{module}.config.SecurityConfig;
import com.{module}.application.service.{ServiceName}Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({Controller}.class)
@Import(SecurityConfig.class)
public class {Controller}Tests {
    @MockitoBean private {ServiceName}Service {serviceName}Service;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should {behavior} and return status code 200")
    void {methodName}TestCase1() throws Exception {
        var response = new {Response}(params);
        var pagedResponse = PagedResponse.<{Response}>builder()
                .page(0).size(20).totalElements(1L).totalPages(1)
                .isLast(true).content(List.of(response)).build();

        when({serviceName}Service.{method}(any())).thenReturn(pagedResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success", "data", pagedResponse
        ));

        mockMvc.perform(get("/api/v1/{endpoint}"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void {methodName}TestCase2() throws Exception {
        mockMvc.perform(get("/api/v1/{endpoint}"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 403 if client is not admin")
    void {methodName}TestCase3() throws Exception {
        mockMvc.perform(get("/api/v1/{endpoint}"))
                .andExpect(status().isForbidden());
    }
}
```

### Complete Test Class Template (JWT-dependent)

```java
package com.{module}.infra.web.catalogue;

import tools.jackson.databind.ObjectMapper;
import com.{module}.config.SecurityConfig;
import com.{module}.application.service.{ServiceName}Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

@WebMvcTest({Controller}.class)
@Import(SecurityConfig.class)
public class {Controller}Tests {
    @MockitoBean private {ServiceName}Service {serviceName}Service;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should {behavior} and return status code 200")
    void {methodName}TestCase1() throws Exception {
        var request = new {Request}(params);
        var response = new {Response}(params);

        when({serviceName}Service.{method}(any())).thenReturn(response);

        String requestBody = objectMapper.writeValueAsString(request);
        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success", "data", response
        ));

        mockMvc.perform(post("/api/v1/{endpoint}")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(List.of(new SimpleGrantedAuthority("SCOPE_USER"))))
        ).andExpect(status().isOk())
          .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void {methodName}TestCase2() throws Exception {
        mockMvc.perform(post("/api/v1/{endpoint}"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 403 if client is not allowed")
    void {methodName}TestCase3() throws Exception {
        mockMvc.perform(post("/api/v1/{endpoint}")
                .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(List.of(new SimpleGrantedAuthority("SCOPE_USER"))))
        ).andExpect(status().isForbidden());
    }
}
```

### Test Naming Convention

- `{methodName}TestCase1` = Success case
- `{methodName}TestCase2` = First exception/error case
- Continue with `TestCase3`, `TestCase4`, etc.

### Authentication Test Cases Summary

| Test Case | SCOPE-only Endpoint | JWT-dependent Endpoint | Expected Status |
|-----------|-------------------|----------------------|-----------------|
| Success | `@WithMockUser(authorities = "SCOPE_ADMIN")` | `@WithMockUser` + `.with(jwt()...)` | 2xx |
| 401 Not Authenticated | No annotation | No annotation | 401 Unauthorized |
| 403 Wrong Scope | `@WithMockUser(authorities = "SCOPE_USER")` | `@WithMockUser` + `.with(jwt()...)` (wrong scope) | 403 Forbidden |
| Business Exception | `@WithMockUser(authorities = "SCOPE_ADMIN")` | `@WithMockUser` + `.with(jwt()...)` | 4xx |

**Key Rule**: EVERY endpoint MUST test both 401 (unauthenticated) and 403 (wrong scope/role).

---

## Strict Pattern for Unit Tests (Services)

Unit tests for application services MUST follow this exact structure. No deviations allowed.

## Test Class Template

```java
package com.{module}.application.service;

import {dto imports};
import {exception imports};
import {entity imports};
import {repository imports};
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import {other imports};

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class {ServiceName}Tests {

    // MOCK ALL DEPENDENCIES, LIKE:
    
    @Mock
    private {DependencyRepository} {dependencyRepository};

    @Mock
    private {DependencyMapper} {dependencyMapper};

    @Mock
    private {OtherDependency} {otherDependency};

    @InjectMocks
    private {Service} {service};
    

    @Test
    @DisplayName("Should {behavior description}")
    void {methodName}TestCase1() {
        // Given
        {Request} request = new {Request}({params});

        {Entity} entity = new {Entity}();
        entity.setId({id});
        entity.setField1("value1");

        {Response} response = new {Response}({responseParams});

        when({mockedDependency}.{method}({args}))
                .thenReturn({mockReturn});
        when({mockedMapper}.{method}({args}))
                .thenReturn({mockReturn});

        // When
        {Result} result = {service}.{method}({params});

        // Then
        assertNotNull(result);
        assertEquals({expected}, result.{field}());
        verify({dependency}).{method}({args});
    }

    @Test
    @DisplayName("Should throw {ExceptionName} when {condition}")
    void {methodName}TestCase2() {
        // Given
        {Request} request = new {Request}({params});

        when({mockedDependency}.{method}({args}))
                .thenReturn({mockReturn});

        // When & Then
        assertThrows({ExceptionName}.class, () -> {
            {service}.{method}({params});
        });

        verify({dependency}).{method}({args});
    }
}
```

## Test Naming Convention

- `{methodName}TestCase1` = Success case
- `{methodName}TestCase2` = Exception case
- Continue with TestCase3, TestCase4, etc.

## Arrange-Given-When-Then Keywords

USE ONLY: `// Given`, `// When`, `// Then`

## Common Test Patterns

### Pattern 1: Success Case with Return

```java
@Test
@DisplayName("Should return expected result")
void methodTestCase1() {
    // Given
    Request request = new Request(param);

    Entity entity = new Entity();
    entity.setId(UUID.randomUUID());
    entity.setName("Test");

    Response response = new Response(entity.getId(), "Test");

    when(repository.findById(any()))
            .thenReturn(Optional.of(entity));
    when(mapper.toResponse(entity))
            .thenReturn(response);

    // When
    Response result = service.method(request);

    // Then
    assertNotNull(result);
    assertEquals("Test", result.name());
    verify(repository).findById(any());
}
```

### Pattern 2: Exception Case

```java
@Test
@DisplayName("Should throw NotFoundException when not found")
void methodTestCase2() {
    // Given
    UUID id = UUID.randomUUID();

    when(repository.findById(id))
            .thenReturn(Optional.empty());

    // When & Then
    assertThrows(NotFoundException.class, () -> {
        service.method(id);
    });

    verify(repository).findById(id);
}
```

### Pattern 3: Pagination

```java
@Test
@DisplayName("Should return paginated results")
void methodTestCase1() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);

    Entity entity = new Entity();
    entity.setId(UUID.randomUUID());

    Page<Entity> page = new PageImpl<>(List.of(entity), pageable, 1);
    Response response = new Response(entity.getId());

    when(repository.findAll(pageable))
            .thenReturn(page);
    when(mapper.toResponse(entity))
            .thenReturn(response);

    // When
    PagedResponse<Response> result = service.getAll(pageable);

    // Then
    assertNotNull(result);
    assertEquals(1, result.totalElements());
    verify(repository).findAll(pageable);
}
```

### Pattern 4: Empty Pagination

```java
@Test
@DisplayName("Should return empty page when no data")
void methodTestCase2() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    Page<Entity> emptyPage = new PageImpl<>(List.of(), pageable, 0);

    when(repository.findAll(pageable))
            .thenReturn(emptyPage);

    // When
    PagedResponse<Response> result = service.getAll(pageable);

    // Then
    assertNotNull(result);
    assertEquals(0, result.totalElements());
    assertTrue(result.content().isEmpty());
    verify(repository).findAll(pageable);
}
```

### Pattern 5: State Change

```java
@Test
@DisplayName("Should update entity state")
void methodTestCase1() {
    // Given
    UUID id = UUID.randomUUID();

    Entity entity = new Entity();
    entity.setId(id);
    entity.setActive(true);

    when(repository.findById(id))
            .thenReturn(Optional.of(entity));
    when(repository.save(entity))
            .thenReturn(entity);

    // When
    service.method(id);

    // Then
    assertFalse(entity.isActive());
    verify(repository).save(entity);
}
```

### Pattern 6: Event Publishing

```java
@Test
@DisplayName("Should publish event on creation")
void methodTestCase1() {
    // Given
    Request request = new Request(param);

    Entity entity = new Entity();
    entity.setId(UUID.randomUUID());

    Response response = new Response(entity.getId());

    when(repository.save(any(Entity.class)))
            .thenReturn(entity);
    when(mapper.toResponse(entity))
            .thenReturn(response);

    // When
    Response result = service.create(request);

    // Then
    assertNotNull(result);
    verify(eventPublisher).publishEvent(any(DomainEvent.class));
}
```

### Pattern 7: Mocking Void Methods

```java
@Test
@DisplayName("Should execute without return")
void methodTestCase1() {
    // Given
    UUID id = UUID.randomUUID();

    Entity entity = new Entity();
    entity.setId(id);

    when(repository.findById(id))
            .thenReturn(Optional.of(entity));
    doNothing().when(repository).delete(entity);

    // When
    service.method(id);

    // Then
    verify(repository).delete(entity);
}
```

## Mockito Methods Reference

### Stubbing
```java
when(mock.method(args)).thenReturn(value);
when(mock.method(args)).thenReturn(Optional.of(entity));
when(mock.method(args)).thenReturn(page);
when(mock.method(args)).thenThrow(new Exception());
when(mock.method(any(Type.class))).thenReturn(value);
```

### Verification
```java
verify(mock).method(args);
verify(mock, never()).method(args);
verify(mock, times(1)).method(args);
verify(mock, atLeastOnce()).method(args);
```

### Argument Matchers
```java
any(), any(UUID.class), any(Type.class)
eq(value)
isNull()
```

### Answer for Dynamic Returns
```java
when(mock.method(any()))
    .thenAnswer(invocation -> {
        Entity e = invocation.getArgument(0);
        e.setId(UUID.randomUUID());
        return e;
    });
```

## No Comments in Test Code

All tests (unit and integration) MUST NOT contain any comments explaining the code. The test name via @DisplayName conveys the intent.

## One Test Per Behavior

Each test method verifies ONE specific behavior. Do not combine multiple assertions for different behaviors.

## Use Package-Wildcard Exception Imports

For cleaner imports in test classes, use wildcard imports for exceptions:

```java
import com.{module}.application.exception.*;
```