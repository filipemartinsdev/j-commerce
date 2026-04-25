# Testing Pattern

## Strict Pattern for Unit Tests

All unit tests MUST follow this exact structure. No deviations allowed.

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

Tests MUST NOT contain any comments explaining the code. The test name via @DisplayName conveys the intent.

## One Test Per Behavior

Each test method verifies ONE specific behavior. Do not combine multiple assertions for different behaviors.