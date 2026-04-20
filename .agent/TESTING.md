# J-Commerce: Unit Testing Standards & Patterns

Complete guide for writing unit tests in J-Commerce following industry best practices and proven patterns.

---

## 🎯 Testing Philosophy

### Core Principles

1. **AAA Pattern** (Arrange → Act → Assert)
   - Clear structure that every test follows
   - Easy to read and understand intent
   - Separates setup from execution from verification

2. **One Assert per Concept**
   - Each test verifies ONE specific behavior
   - Multiple assertions are OK if testing the same concept
   - Prevents multi-concept tests

3. **Meaningful Names**
   - Test names describe WHAT is being tested and WHAT result is expected
   - Follow pattern: `testMethodName_Condition_ExpectedResult()`
   - Use `@DisplayName` for human-readable descriptions

4. **Isolation**
   - Each test is independent
   - No shared state between tests
   - Mock all external dependencies

5. **Fast Execution**
   - Tests should run in milliseconds
   - Use mocks, not real databases
   - No network calls or I/O operations

---

## 🏗️ Unit Test Structure

### Recommended Test Class Organization

```
Test Class Structure:
├── Imports (organized)
├── @ExtendWith(MockitoExtension.class)
├── @Mock dependencies
├── @InjectMocks subject under test
├── Test method 1 (@Test, @DisplayName)
├── Test method 2
└── Test method N
```

**Key Points:**
- One test class per service class
- Use `@Mock` for all external dependencies
- Use `@InjectMocks` for the service being tested
- Use Mockito for stubbing and verification

---

## 📋 Service Testing Template

### Complete Example: AdminProductServiceTests

This is the **standard template** for all service tests in J-Commerce. Follow this pattern exactly.

```java
package com.products.application.service;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.admin.*;
import com.products.application.event.ProductSKUCreatedEvent;
import com.products.application.event.ProductSKUDeletedEvent;
import com.products.application.exception.*;
import com.products.application.service.mapper.ProductAdminMapper;
import com.products.application.service.mapper.ProductSKUAdminMapper;
import com.products.domain.entity.Product;
import com.products.domain.entity.ProductCategory;
import com.products.domain.entity.ProductSKU;
import com.products.infra.persistence.ProductCategoryRepository;
import com.products.infra.persistence.ProductRepository;
import com.products.infra.persistence.ProductSKURepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminProductServiceTests {
    
    // ============================================================
    // MOCKS & INJECTIONS
    // ============================================================
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private ProductCategoryRepository productCategoryRepository;
    
    @Mock
    private ProductAdminMapper productAdminMapper;
    
    @Mock
    private ProductSKURepository productSKURepository;
    
    @Mock
    private ProductSKUAdminMapper productSKUAdminMapper;
    
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    
    @InjectMocks
    private AdminProductService adminProductService;
    
    // ============================================================
    // SUCCESS CASE TESTS
    // ============================================================
    
    @Test
    @DisplayName("Should create new product and retrieve it DTO successfully if everything is OK")
    void createProductTestCase1() {
        // ARRANGE - Set up test data
        ProductCategory category = new ProductCategory();
        category.setId(1);
        category.setName("Electronics");
        
        CreateProductRequest request = new CreateProductRequest(
            "Laptop", 
            Optional.of("Gaming Laptop"), 
            1
        );
        
        Product productEntity = new Product();
        productEntity.setId(UUID.randomUUID());
        productEntity.setName("Laptop");
        productEntity.setDescription("Gaming Laptop");
        productEntity.setCategory(category);
        productEntity.setActive(true);
        
        ProductAdminResponse response = new ProductAdminResponse(
            productEntity.getId(),
            "Laptop",
            "Gaming Laptop",
            Instant.now(),
            Instant.now()
        );
        
        // Mock expectations
        when(productCategoryRepository.findById(1))
            .thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class)))
            .thenReturn(productEntity);
        when(productAdminMapper.toResponse(productEntity))
            .thenReturn(response);
        
        // ACT - Execute the method
        ProductAdminResponse result = adminProductService.createProduct(request);
        
        // ASSERT - Verify result
        assertNotNull(result);
        assertEquals("Laptop", result.name());
        assertEquals("Gaming Laptop", result.description());
        
        // Verify interactions
        verify(productCategoryRepository).findById(1);
        verify(productRepository).save(any(Product.class));
        verify(productAdminMapper).toResponse(productEntity);
    }
    
    // ============================================================
    // FAILURE/EXCEPTION CASE TESTS
    // ============================================================
    
    @Test
    @DisplayName("Should InvalidProductCategoryException if categoryId is invalid")
    void createProductTestCase2() {
        // ARRANGE
        CreateProductRequest request = new CreateProductRequest(
            "Laptop", 
            Optional.of("Gaming Laptop"), 
            999
        );
        
        when(productCategoryRepository.findById(999))
            .thenReturn(Optional.empty());
        
        // ACT & ASSERT
        assertThrows(InvalidProductCategoryException.class, () -> {
            adminProductService.createProduct(request);
        });
        
        // Verify that repository and save were called
        verify(productCategoryRepository).findById(999);
        verify(productRepository, never()).save(any());
    }
}
```

---

## 🔑 Key Test Patterns

### Pattern 1: Testing Success Cases

**Structure**: Given valid input → When method called → Then return expected result

```java
@Test
@DisplayName("Should create product and return response successfully")
void testCreateProductSuccess() {
    // ARRANGE - Create valid request and mock dependencies
    CreateProductRequest request = new CreateProductRequest("Laptop", Optional.of("Gaming"), 1);
    Product savedProduct = new Product();
    // ... setup ...
    
    when(categoryRepository.findById(1))
        .thenReturn(Optional.of(category));
    when(productRepository.save(any(Product.class)))
        .thenReturn(savedProduct);
    when(mapper.toResponse(savedProduct))
        .thenReturn(response);
    
    // ACT - Execute method
    ProductResponse result = service.createProduct(request);
    
    // ASSERT - Verify result meets expectations
    assertNotNull(result);
    assertEquals("Laptop", result.name());
    assertEquals("Gaming", result.description());
    
    // Verify all mocks were called correctly
    verify(categoryRepository).findById(1);
    verify(productRepository).save(any(Product.class));
}
```

### Pattern 2: Testing Exception Cases

**Structure**: Given invalid input → When method called → Then throw expected exception

```java
@Test
@DisplayName("Should throw ProductNotFoundException when product not found")
void testGetProductNotFound() {
    // ARRANGE
    UUID productId = UUID.randomUUID();
    when(productRepository.findById(productId))
        .thenReturn(Optional.empty());
    
    // ACT & ASSERT - Exception should be thrown
    assertThrows(ProductNotFoundException.class, () -> {
        service.getProductById(productId);
    });
    
    // Verify repository was called
    verify(productRepository).findById(productId);
}
```

### Pattern 3: Testing Pagination

**Structure**: Given pageable request → When method called → Then return PagedResponse

```java
@Test
@DisplayName("Should retrieve all products with pagination")
void testGetAllProductsWithPagination() {
    // ARRANGE
    Pageable pageable = PageRequest.of(0, 10);
    
    Product product1 = new Product();
    product1.setId(UUID.randomUUID());
    product1.setName("Product 1");
    
    Product product2 = new Product();
    product2.setId(UUID.randomUUID());
    product2.setName("Product 2");
    
    Page<Product> page = new PageImpl<>(
        List.of(product1, product2), 
        pageable, 
        2
    );
    
    ProductResponse response1 = new ProductResponse(...);
    ProductResponse response2 = new ProductResponse(...);
    
    when(repository.findAll(pageable))
        .thenReturn(page);
    when(mapper.toResponse(product1))
        .thenReturn(response1);
    when(mapper.toResponse(product2))
        .thenReturn(response2);
    
    // ACT
    PagedResponse<ProductResponse> result = service.getAllProducts(pageable);
    
    // ASSERT
    assertNotNull(result);
    assertEquals(0, result.page());
    assertEquals(10, result.size());
    assertEquals(2, result.totalElements());
    assertEquals(1, result.totalPages());
    assertTrue(result.isLast());
    assertEquals(2, result.content().size());
    
    verify(repository).findAll(pageable);
}
```

### Pattern 4: Testing Event Publishing

**Structure**: Given service operation → When method called → Then event published

```java
@Test
@DisplayName("Should publish ProductSKUCreatedEvent when SKU created")
void testCreateProductSKUPublishesEvent() {
    // ARRANGE
    UUID productId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    CreateProductSKURequest request = new CreateProductSKURequest(
        productId, 
        "SKU-001", 
        "SKU Name"
    );
    
    Product product = new Product();
    product.setId(productId);
    product.setActive(true);
    
    ProductSKU newSKU = new ProductSKU();
    newSKU.setId(UUID.randomUUID());
    newSKU.setProduct(product);
    newSKU.setSKU("SKU-001");
    newSKU.setName("SKU Name");
    newSKU.setIsActive(true);
    
    ProductSKUResponse response = new ProductSKUResponse(...);
    
    when(productRepository.findById(productId))
        .thenReturn(Optional.of(product));
    when(productSKURepository.save(any(ProductSKU.class)))
        .thenReturn(newSKU);
    when(mapper.toResponse(newSKU))
        .thenReturn(response);
    
    // ACT
    ProductSKUResponse result = service.createProductSKU(request, userId);
    
    // ASSERT
    assertNotNull(result);
    assertEquals("SKU-001", result.SKU());
    
    // Verify event was published
    verify(eventPublisher).publishEvent(any(ProductSKUCreatedEvent.class));
    
    verify(productRepository).findById(productId);
    verify(productSKURepository).save(any(ProductSKU.class));
}
```

### Pattern 5: Testing Empty Results

**Structure**: Given no data → When method called → Then return empty collection

```java
@Test
@DisplayName("Should return empty PagedResponse when no products exist")
void testGetAllProductsEmpty() {
    // ARRANGE
    Pageable pageable = PageRequest.of(0, 10);
    Page<Product> emptyPage = new PageImpl<>(
        Collections.emptyList(), 
        pageable, 
        0
    );
    
    when(repository.findAll(pageable))
        .thenReturn(emptyPage);
    
    // ACT
    PagedResponse<ProductResponse> result = service.getAllProducts(pageable);
    
    // ASSERT
    assertNotNull(result);
    assertEquals(0, result.totalElements());
    assertTrue(result.content().isEmpty());
    
    verify(repository).findAll(pageable);
}
```

### Pattern 6: Testing State Changes

**Structure**: Given entity with state → When method called → Then state is modified correctly

```java
@Test
@DisplayName("Should mark product as inactive and publish event")
void testDeleteProductMarksInactive() {
    // ARRANGE
    UUID productId = UUID.randomUUID();
    
    Product product = new Product();
    product.setId(productId);
    product.setName("Laptop");
    product.setActive(true);
    product.setSKUs(new ArrayList<>()); // No active SKUs
    
    when(repository.findById(productId))
        .thenReturn(Optional.of(product));
    
    // ACT
    service.deleteProductById(productId);
    
    // ASSERT - Verify state changed
    assertFalse(product.isActive());
    
    // Verify save was called
    verify(repository).save(product);
}
```

---

## 📊 Test Case Organization

### For Each Service Method: Write 2-4 Test Cases

**Template:**

| Case # | Scenario | Expected | Example |
|--------|----------|----------|---------|
| 1 | Success - Happy path | Returns correct result | Create product with valid input → DTO returned |
| 2 | Validation fails | Throws specific exception | Invalid category → InvalidProductCategoryException |
| 3 | Resource not found | Throws NotFoundException | ID not exists → ProductNotFoundException |
| 4 | Edge case (if needed) | Specific behavior | Empty list → Returns empty PagedResponse |

### Example: Method `createProduct()`

```java
// Test Case 1: Success
@Test
@DisplayName("Should create product and return DTO successfully")
void createProductSuccess() { ... }

// Test Case 2: Invalid Category
@Test
@DisplayName("Should throw InvalidProductCategoryException for invalid categoryId")
void createProductInvalidCategory() { ... }
```

---

## ✅ Test Writing Checklist

Before you submit a test, verify:

- [ ] **Naming**: Test name clearly describes scenario and expected result
- [ ] **@DisplayName**: Human-readable name explains the test
- [ ] **AAA Pattern**: Clear Arrange/Act/Assert separation
- [ ] **One Concept**: Each test verifies one behavior
- [ ] **Mocks**: All external dependencies are mocked
- [ ] **No Assertions**: No real database or network calls
- [ ] **Setup**: All required mocks are set up in Arrange phase
- [ ] **Verification**: Use `verify()` to ensure mocks were called
- [ ] **Edge Cases**: Test both success and failure paths
- [ ] **Isolation**: No dependencies between tests
- [ ] **Clear Assertions**: `assertEquals()`, `assertTrue()`, `assertThrows()` clearly show what's expected
- [ ] **Never(): No missed mocks** - Use `verify(..., never())` to ensure unused mocks aren't called

---

## 🚫 Anti-Patterns to Avoid

| ❌ Anti-Pattern | ✅ Instead | Why |
|---|---|---|
| Test uses real database | Mock repository with `@Mock` | Tests must be fast and isolated |
| Multiple concepts per test | Split into separate tests | One concept per test = easier debugging |
| Unclear test names | `testCreateProductValidInput_Success()` | Name describes scenario and result |
| No assertions | Add explicit `assertEquals()`, `assertTrue()` | Clear verification of behavior |
| Complex Arrange phase | Use helper methods or builders | Arrange should be simple and readable |
| Shared state between tests | Each test sets up its own data | Tests must be independent |
| Testing implementation details | Test behavior/contract | Tests should be resistant to refactoring |
| No mock verification | Use `verify()` to ensure mocks called | Confirms proper interactions |
| Generic test data | Use realistic, descriptive values | Makes tests easier to understand |
| Ignoring exceptions | Test that exceptions ARE thrown | Exception behavior is part of contract |

---

## 🔧 Mockito Usage Guide

### Stubbing (Setting up mock behavior)

```java
// Return Optional value
when(repository.findById(id))
    .thenReturn(Optional.of(entity));

// Return empty Optional (not found)
when(repository.findById(id))
    .thenReturn(Optional.empty());

// Return specific value
when(mapper.toResponse(entity))
    .thenReturn(response);

// Throw exception
when(repository.findById(id))
    .thenThrow(new EntityNotFoundException());

// Multiple return values (for multiple calls)
when(service.getValue())
    .thenReturn(1)
    .thenReturn(2)
    .thenReturn(3);

// Match any argument
when(repository.save(any(Product.class)))
    .thenReturn(savedProduct);

// Match specific types
when(repository.findAll(any(Pageable.class)))
    .thenReturn(page);
```

### Verification (Checking mock interactions)

```java
// Verify method was called
verify(repository).findById(id);

// Verify method was called with specific argument
verify(repository).findById(productId);

// Verify method was never called
verify(repository, never()).save(any());

// Verify method called exact number of times
verify(repository, times(1)).findById(id);

// Verify exact order of calls
InOrder inOrder = inOrder(repo1, repo2);
inOrder.verify(repo1).method1();
inOrder.verify(repo2).method2();
```

---

## 📦 Required Dependencies

Add to `pom.xml`:

```xml
<!-- JUnit 5 (Jupiter) -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<!-- Mockito -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>

<!-- Mockito JUnit 5 Integration -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<!-- Spring Test (if using Spring components) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 🎓 Recommended Test Coverage

- **Service Layer**: 80-100% coverage (easy to test with mocks)
- **Controller Layer**: 60-80% coverage (requires MockMvc setup)
- **Domain Layer**: 80-100% coverage (pure logic, no dependencies)
- **Repository Layer**: 30-50% coverage (depends on database)
- **Infra/Config**: 20-40% coverage (hard to test, less critical)

**Total Target**: 70-80% code coverage

---

## 🚀 Quick Start: Creating a New Test Class

### Step-by-Step Template

1. **Create test class** in `src/test/java/` same package as service
2. **Add extension**: `@ExtendWith(MockitoExtension.class)`
3. **Add mocks**: `@Mock` for all dependencies
4. **Add service**: `@InjectMocks` for service being tested
5. **Write test method**: 
   - `@Test @DisplayName("Description")`
   - void methodName() with AAA pattern
6. **Run**: Maven `mvn test -Dtest=YourTestClass`

```java
@ExtendWith(MockitoExtension.class)
public class ProductServiceTests {
    
    @Mock
    private ProductRepository repository;
    
    @Mock
    private ProductMapper mapper;
    
    @InjectMocks
    private ProductService service;
    
    @Test
    @DisplayName("Should create product successfully")
    void testCreateProduct() {
        // ARRANGE
        CreateProductRequest request = new CreateProductRequest(...);
        Product product = new Product();
        ProductResponse response = new ProductResponse(...);
        
        when(repository.save(any(Product.class)))
            .thenReturn(product);
        when(mapper.toResponse(product))
            .thenReturn(response);
        
        // ACT
        ProductResponse result = service.createProduct(request);
        
        // ASSERT
        assertNotNull(result);
        assertEquals("Expected", result.name());
        verify(repository).save(any(Product.class));
    }
}
```

---

## 🔍 Debugging Failed Tests

### Common Issues

**Issue**: `NullPointerException in test`
- **Cause**: Mock not configured for that scenario
- **Fix**: Add `when(...).thenReturn(...)` for all mock calls

**Issue**: `Unnecessary stubbings detected`
- **Cause**: Mock configured but method not actually called
- **Fix**: Remove unused `when()` stubs or verify mock is called

**Issue**: `No Such Element Exception`
- **Cause**: `Optional.get()` called on empty Optional
- **Fix**: Use `.orElse()`, `.orElseThrow()`, or `.ifPresent()` instead

**Issue**: `Expected exception not thrown`
- **Cause**: Method doesn't validate input properly
- **Fix**: Check service logic or test is checking wrong scenario

**Issue**: `AssertionError: Expected X, got Y`
- **Cause**: Actual result doesn't match expected
- **Fix**: Check mock setup or service logic

---

## 📚 References

- **AdminProductServiceTests.java** - Complete real-world example
- **JUnit 5 Documentation**: https://junit.org/junit5/docs/current/user-guide/
- **Mockito Documentation**: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
- **Testing Best Practices**: See DEVELOPMENT.md - Code Review Checklist

---

## 🎯 Success Criteria

After writing tests, you should be able to answer:

- [ ] Can I describe what each test verifies?
- [ ] Are all tests independent (no shared state)?
- [ ] Does each test name clearly describe the scenario?
- [ ] Are all external dependencies mocked?
- [ ] Does each test have clear Arrange/Act/Assert phases?
- [ ] Are success AND failure cases tested?
- [ ] Do tests run in under 100ms total?

**If yes to all**: Your tests are ready! 🎉

---

**Status**: ✅ Complete  
**Last Updated**: April 2026  
**Example File**: `AdminProductServiceTests.java` (25 tests, 100% passing)
