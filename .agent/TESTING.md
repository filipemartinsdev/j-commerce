# Testing Pattern

## Strict Pattern for Integration Tests (Web MVC)

Integration tests for web controllers MUST follow this exact structure. No deviations allowed.

> **Note**: The package path `com.{module}.security.infra.web` and references to `AuthService` in this document are specific to the authentication microservice context. For other modules, adjust the package and service accordingly to match your domain.

### Test Class Template

```java
package com.{module}.{domain}.infra.web;

import com.identity.config.SecurityConfig;
import com.{module}.{domain}.application.dto.{DtoImport};
import com.{module}.{domain}.application.exception.{ExceptionImport};
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({Controller}.class)
@Import(SecurityConfig.class)
public class {Controller}Tests {
    @Autowired private MockMvc mockMvc;

    @MockitoBean private {ServiceName}Service {serviceName}Service;
}
```

### Annotations Reference

| Annotation | Purpose |
|------------|---------|
| `@WebMvcTest(Controller.class)` | Tests only the web layer |
| `@Import(SecurityConfig.class)` | Import security configuration |
| `@MockitoBean` | Mock the service dependency |
| `@WithMockUser(authorities = "SCOPE_ADMIN")` | Simulate authenticated admin user |
| `@ActiveProfiles("test")` | Use test profile when needed |

### Case 1: Success Case (Authenticated User with Proper Role)

```java
@Test @DisplayName("Should {behavior description}")
@WithMockUser(authorities = "SCOPE_ADMIN")
void {methodName}TestCase1() throws Exception {
    String requestBody = """
        {
            "field": "value"
        }
    """;

    doNothing().when({serviceName}Service).{method}(any());

    mockMvc.perform({httpMethod}("/api/v1/{endpoint}")
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk());
}
```

### Case 2: Exception Case (Business Logic Error)

```java
@Test @DisplayName("Should return status code {code} if {condition}")
@WithMockUser(authorities = "SCOPE_ADMIN")
void {methodName}TestCase2() throws Exception {
    String requestBody = """
        {
            "field": "value"
        }
    """;

    doThrow({ExceptionName}.class).when({serviceName}Service).{method}(any());

    mockMvc.perform({httpMethod}("/api/v1/{endpoint}")
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().is{StatusCode}());
}
```

### Case 3: Not Authenticated (401 Unauthorized)

```java
@Test @DisplayName("Should return response code 401 if client is not authenticated")
void {methodName}TestCase{N}() throws Exception {
    mockMvc.perform({httpMethod}("/api/v1/{endpoint}")
    ).andExpect(status().isUnauthorized());
}
```

### Case 4: Forbidden (403 - Wrong Role)

```java
@Test @DisplayName("Should return response code 403 if client hasn't ADMIN authorities")
@WithMockUser(authorities = "SCOPE_USER")
void {methodName}TestCase{N}() throws Exception {
    mockMvc.perform({httpMethod}("/api/v1/{endpoint}")
    ).andExpect(status().isForbidden());
}
```

### Case 5: Success Response with JSON Body

```java
@Test @DisplayName("Should {behavior description}")
@WithMockUser(authorities = "SCOPE_ADMIN")
void {methodName}TestCase1() throws Exception {
    String requestBody = """
        {
            "field": "value"
        }
    """;

    {Response} response = new {Response}({params});

    String expectedJSON = """
        {
            "status": "success",
            "data": {
                "field": "%s"
            }
        }
    """.formatted(response.field());

    when({serviceName}Service.{method}(any()))
            .thenReturn(response);

    mockMvc.perform({httpMethod}("/api/v1/{endpoint}")
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk())
    .andExpect(content().json(expectedJSON));
}
```

### Case 6: Paged Response

```java
@Test @DisplayName("Should retrieve all {entity} and return status code 200")
@WithMockUser(authorities = "SCOPE_ADMIN")
void getAllTestCase1() throws Exception {
    UUID entityId = UUID.randomUUID();

    PagedResponse<{Response}> serviceResponse = PagedResponse.<{Response}>builder()
            .page(0)
            .size(20)
            .totalPages(1)
            .totalElements(1L)
            .isLast(true)
            .content(List.of(new {Response}(entityId, "value")))
            .build();

    String expectedResponse = """
        {
            "status": "success",
            "data": {
                "page": 0,
                "size": 20,
                "totalPages": 1,
                "totalElements": 1,
                "isLast": true,
                "content": [
                    {
                        "field": "%s"
                    }
                ]
            }
        }
    """.formatted(entityId.toString());

    when({serviceName}Service.getAll(any()))
            .thenReturn(serviceResponse);

    mockMvc.perform(get("/api/v1/{endpoint}"))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedResponse));
}
```

### Test Naming Convention

- `{methodName}TestCase1` = Success case
- `{methodName}TestCase2` = Exception case
- Continue with TestCase3, TestCase4, etc.

### Authentication Test Cases Summary

| Test Case | Annotation | Expected Status |
|-----------|------------|-----------------|
| Success (valid user + role) | `@WithMockUser(authorities = "SCOPE_ADMIN")` | 2xx |
| Not authenticated | None | 401 Unauthorized |
| Wrong role | `@WithMockUser(authorities = "SCOPE_USER")` | 403 Forbidden |
| Business exception | `@WithMockUser(authorities = "SCOPE_ADMIN")` | 4xx |

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