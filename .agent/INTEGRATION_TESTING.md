# J-Commerce: Integration Testing Standards & Patterns

Complete guide for writing integration tests in J-Commerce covering Repository, Controller, and multi-layer tests.

---

## 🔄 Integration Testing Philosophy

### Core Differences from Unit Tests

| Aspect | Unit Tests (TESTING.md) | Integration Tests |
|--------|------------------------|--------------------|
| **Scope** | Single service with mocks | Multiple layers (Database, HTTP) |
| **Database** | None (mocked) | Real or Embedded (H2) |
| **Speed** | Very fast (ms) | Slower (seconds) |
| **Purpose** | Test logic isolation | Test components working together |
| **Setup** | Mockito @Mock/@InjectMocks | Spring Test Context, @DataJpaTest |
| **Example** | Service method logic | Repository + Entity constraints |
| **When to use** | Unit logic testing | Database queries, constraints, persistence |

---

## 🗂️ Types of Integration Tests

### 1. **Repository Integration Tests** (@DataJpaTest)
**Purpose**: Verify database queries, constraints, and persistence

**Characteristics**:
- Uses embedded H2 database
- Only loads JPA components (Entities, Repositories)
- Lightning fast compared to full context tests
- Tests database constraints, query correctness

### 2. **Service + Repository Integration Tests**
**Purpose**: Verify service logic with real database operations

**Characteristics**:
- Uses @SpringBootTest or sliced context
- Tests transactional behavior
- Verifies business logic with persistence
- Tests event publishing with real database

### 3. **Controller Integration Tests** (@WebMvcTest or @SpringBootTest)
**Purpose**: Verify HTTP layer, validation, error handling

**Characteristics**:
- Uses MockMvc for HTTP testing
- Tests request/response serialization
- Verifies HTTP status codes and headers
- Tests exception handling in controllers

### 4. **End-to-End Tests** (@SpringBootTest)
**Purpose**: Full stack testing with all components

**Characteristics**:
- Entire Spring context loaded
- Real HTTP client or TestRestTemplate
- Tests service-to-service communication
- Most realistic but slowest

---

## 📋 Repository Integration Tests

### Template: Complete Repository Test Class

```java
package com.products.infra.persistence;

import com.products.domain.entity.Product;
import com.products.domain.entity.ProductCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class ProductRepositoryTests {
    
    // ============================================================
    // DEPENDENCIES
    // ============================================================
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductCategoryRepository categoryRepository;
    
    // ============================================================
    // SAVE & RETRIEVE
    // ============================================================
    
    @Test
    @DisplayName("Should save product and retrieve it successfully")
    void testSaveProductSuccess() {
        // ARRANGE
        ProductCategory category = new ProductCategory();
        category.setId(1);
        category.setName("Electronics");
        
        Product product = new Product();
        product.setName("Laptop");
        product.setDescription("Gaming Laptop");
        product.setCategory(category);
        product.setActive(true);
        
        // ACT
        Product savedProduct = productRepository.save(product);
        
        // ASSERT
        assertNotNull(savedProduct.getId());
        assertEquals("Laptop", savedProduct.getName());
        assertEquals("Gaming Laptop", savedProduct.getDescription());
        assertTrue(savedProduct.isActive());
    }
    
    @Test
    @DisplayName("Should retrieve product by ID")
    void testFindProductById() {
        // ARRANGE - Save first
        Product product = new Product();
        product.setName("Monitor");
        product.setActive(true);
        Product saved = productRepository.save(product);
        
        // ACT
        Optional<Product> found = productRepository.findById(saved.getId());
        
        // ASSERT
        assertTrue(found.isPresent());
        assertEquals("Monitor", found.get().getName());
    }
    
    // ============================================================
    // CONSTRAINTS TESTING
    // ============================================================
    
    @Test
    @DisplayName("Should fail when saving product without name")
    void testSaveProductWithoutName() {
        // ARRANGE
        Product product = new Product();
        product.setDescription("No name product");
        product.setActive(true);
        
        // ACT & ASSERT - Should throw validation exception
        assertThrows(Exception.class, () -> {
            productRepository.save(product);
            productRepository.flush(); // Force validation
        });
    }
    
    @Test
    @DisplayName("Should fail when category is null")
    void testSaveProductWithoutCategory() {
        // ARRANGE
        Product product = new Product();
        product.setName("Product");
        product.setDescription("No category");
        product.setCategory(null); // Violation
        product.setActive(true);
        
        // ACT & ASSERT
        assertThrows(Exception.class, () -> {
            productRepository.save(product);
            productRepository.flush();
        });
    }
    
    // ============================================================
    // CUSTOM QUERY TESTING
    // ============================================================
    
    @Test
    @DisplayName("Should retrieve all active products")
    void testFindAllActive() {
        // ARRANGE - Create mixed products
        Product active1 = new Product();
        active1.setName("Active 1");
        active1.setActive(true);
        productRepository.save(active1);
        
        Product active2 = new Product();
        active2.setName("Active 2");
        active2.setActive(true);
        productRepository.save(active2);
        
        Product inactive = new Product();
        inactive.setName("Inactive");
        inactive.setActive(false);
        productRepository.save(inactive);
        
        // ACT
        var allActive = productRepository.findAll();
        
        // ASSERT - Manual filter required (no @SQLRestriction on Product entity)
        assertNotNull(allActive);
        assertTrue(allActive.stream()
            .allMatch(Product::isActive));
    }
    
    // ============================================================
    // RELATIONSHIP TESTING
    // ============================================================
    
    @Test
    @DisplayName("Should cascade delete SKUs when product deleted")
    void testProductSkuCascade() {
        // ARRANGE
        ProductCategory category = new ProductCategory();
        category.setId(1);
        
        Product product = new Product();
        product.setName("Laptop");
        product.setCategory(category);
        product.setActive(true);
        Product saved = productRepository.save(product);
        
        // Create SKU (setup relationship)
        // ... (depends on ProductSKU structure)
        
        // ACT
        productRepository.delete(saved);
        
        // ASSERT
        assertFalse(productRepository.findById(saved.getId()).isPresent());
    }
}
```

---

## 🔑 Key Annotations for Integration Tests

### @DataJpaTest
```java
@DataJpaTest
public class ProductRepositoryTests {
    // Only loads JPA components
    // Uses embedded H2 database
    // Fast compared to full context
}
```

**What it loads:**
- ✅ Entity classes
- ✅ Repository interfaces
- ✅ JPA configuration
- ❌ Services
- ❌ Controllers
- ❌ Event listeners

### @ActiveProfiles("test")
```java
@DataJpaTest
@ActiveProfiles("test")
public class ProductRepositoryTests {
    // Uses application-test.yaml configuration
}
```

**Purpose:**
- Load test-specific configuration
- Override database connection
- Use H2 instead of PostgreSQL
- Load test data if needed

**application-test.yaml example:**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
```

### @SpringBootTest
```java
@SpringBootTest
@ActiveProfiles("test")
public class ProductServiceIntegrationTests {
    // Loads entire Spring context
    // Use for service + repository testing
    // Slower but more realistic
}
```

---

## 🎯 Common Integration Test Patterns

### Pattern 1: Testing Repository Query Methods

```java
@DataJpaTest
@ActiveProfiles("test")
public class ProductRepositoryQueryTests {
    
    @Autowired
    private ProductRepository repository;
    
    @Test
    @DisplayName("Should find products by category")
    void testFindByCategoryId() {
        // ARRANGE
        ProductCategory category = new ProductCategory();
        category.setId(1);
        
        Product product1 = new Product();
        product1.setName("Product 1");
        product1.setCategory(category);
        product1.setActive(true);
        repository.save(product1);
        
        Product product2 = new Product();
        product2.setName("Product 2");
        product2.setCategory(category);
        product2.setActive(true);
        repository.save(product2);
        
        // ACT
        List<Product> result = repository.findByCategoryIdAndIsActiveTrue(1);
        
        // ASSERT
        assertEquals(2, result.size());
        assertTrue(result.stream()
            .allMatch(p -> p.getCategory().getId().equals(1)));
    }
}
```

### Pattern 2: Testing Constraints & Validation

```java
@DataJpaTest
@ActiveProfiles("test")
public class ProductConstraintTests {
    
    @Autowired
    private ProductRepository repository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    @DisplayName("Should enforce unique SKU constraint")
    void testUniqueSKUConstraint() {
        // ARRANGE
        ProductSKU sku1 = new ProductSKU();
        sku1.setSKU("SKU-001");
        sku1.setName("First SKU");
        repository.saveProductSKU(sku1);
        
        ProductSKU sku2 = new ProductSKU();
        sku2.setSKU("SKU-001"); // Duplicate
        sku2.setName("Second SKU");
        
        // ACT & ASSERT
        assertThrows(DataIntegrityViolationException.class, () -> {
            repository.saveProductSKU(sku2);
            entityManager.flush();
        });
    }
}
```

### Pattern 3: Testing Lazy Loading & Relationships

```java
@DataJpaTest
@ActiveProfiles("test")
public class ProductLazyLoadingTests {
    
    @Autowired
    private ProductRepository repository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    @DisplayName("Should not raise LazyInitializationException")
    void testLazyLoadingWithinTransaction() {
        // ARRANGE
        Product product = new Product();
        product.setName("Laptop");
        repository.save(product);
        
        // ACT - Clear cache to force reload
        entityManager.clear();
        Optional<Product> loaded = repository.findById(product.getId());
        
        // ASSERT - Access lazy loaded collection within transaction
        assertTrue(loaded.isPresent());
        assertNotNull(loaded.get().getSKUs()); // Should not throw
    }
}
```

---

## 🧪 Controller Integration Tests

### Template: Controller Test with MockMvc

```java
package com.products.infra.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.products.application.dto.admin.CreateProductRequest;
import com.products.application.service.AdminProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminProductController.class)
public class AdminProductControllerTests {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private AdminProductService service;
    
    // ============================================================
    // SUCCESS CASE TESTS
    // ============================================================
    
    @Test
    @DisplayName("Should create product and return 201 CREATED")
    void testCreateProductSuccess() throws Exception {
        // ARRANGE
        CreateProductRequest request = new CreateProductRequest(
            "Laptop",
            Optional.of("Gaming Laptop"),
            1
        );
        
        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/admin/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @DisplayName("Should retrieve product and return 200 OK")
    void testGetProductSuccess() throws Exception {
        // ARRANGE
        UUID productId = UUID.randomUUID();
        
        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/admin/products/{id}", productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
    
    // ============================================================
    // FAILURE CASE TESTS
    // ============================================================
    
    @Test
    @DisplayName("Should return 404 NOT FOUND for non-existent product")
    void testGetProductNotFound() throws Exception {
        // ARRANGE
        UUID productId = UUID.randomUUID();
        
        when(service.getProductById(any()))
            .thenThrow(new ProductNotFoundException("Not found"));
        
        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/admin/products/{id}", productId))
            .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Should return 400 BAD REQUEST for invalid input")
    void testCreateProductInvalidRequest() throws Exception {
        // ARRANGE
        String invalidJson = "{\"name\": \"\"}"; // Empty name
        
        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/admin/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should return 409 CONFLICT for duplicate")
    void testCreateProductDuplicate() throws Exception {
        // ARRANGE
        CreateProductRequest request = new CreateProductRequest(
            "Laptop",
            Optional.of("Gaming"),
            1
        );
        
        when(service.createProduct(any()))
            .thenThrow(new ProductAlreadyExistsException("Duplicate"));
        
        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/admin/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }
    
    // ============================================================
    // VALIDATION TESTS
    // ============================================================
    
    @Test
    @DisplayName("Should validate request and return error details")
    void testValidationErrorResponse() throws Exception {
        // ARRANGE
        String invalidRequest = "{}"; // Missing required fields
        
        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/admin/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errors").isArray());
    }
}
```

---

## 🔗 Service + Repository Integration Tests

### Full Stack Service Test with Database

```java
@SpringBootTest
@ActiveProfiles("test")
public class AdminProductServiceIntegrationTests {
    
    @Autowired
    private AdminProductService service;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductCategoryRepository categoryRepository;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Test
    @DisplayName("Should create product with real database persistence")
    @Transactional
    void testCreateProductWithDatabase() {
        // ARRANGE
        ProductCategory category = new ProductCategory();
        category.setId(1);
        categoryRepository.save(category);
        
        CreateProductRequest request = new CreateProductRequest(
            "Laptop",
            Optional.of("Gaming"),
            1
        );
        
        // ACT
        ProductAdminResponse result = service.createProduct(request);
        
        // ASSERT - Verify in database
        assertNotNull(result.id());
        Optional<Product> saved = productRepository.findById(result.id());
        assertTrue(saved.isPresent());
        assertEquals("Laptop", saved.get().getName());
    }
}
```

---

## ✅ Integration Test Checklist

Before submitting integration tests:

- [ ] **Correct Annotation**: @DataJpaTest or @SpringBootTest
- [ ] **@ActiveProfiles("test")**: Using test configuration
- [ ] **Database Setup**: H2 configured in application-test.yaml
- [ ] **Assertions**: Verify both result AND database state
- [ ] **Constraints Tested**: Unique, NotNull, relationships
- [ ] **Error Handling**: Test validation failures
- [ ] **Cleanup**: Tests clean up after themselves (auto with Spring)
- [ ] **Isolation**: Each test independent
- [ ] **MockMvc**: Used for controller tests
- [ ] **TestEntityManager**: Used for advanced assertions

---

## 🚫 Anti-Patterns in Integration Tests

| ❌ Anti-Pattern | ✅ Instead | Why |
|---|---|---|
| Using production database | Use embedded H2 in tests | Tests must be fast and isolated |
| No @ActiveProfiles("test") | Always add @ActiveProfiles | Ensures correct config loaded |
| Testing without @Transactional | Add @Transactional | Isolates changes between tests |
| Not clearing database between tests | Use test properties | Prevents cross-test pollution |
| Checking only result, not DB | Query database and verify | Confirms actual persistence |
| Testing without assertions | Add clear assertions | Verifies behavior |
| Very slow integration tests | Use @DataJpaTest for repos | Not all tests need full context |
| Mocking repositories in service tests | Use @SpringBootTest if integration test | Either mock everything OR test with real DB |

---

## 📊 When to Use Each Test Type

| Test Type | Uses Mocks? | Database? | Speed | Coverage | When to Use |
|-----------|-----------|----------|-------|----------|------------|
| **Unit (TESTING.md)** | All mocked | No | Fastest | Single class | Business logic, no I/O |
| **Repo (this)** | No mocks | H2 real | Very fast | Queries & constraints | Repository logic, JPA |
| **Service + Repo** | No mocks | H2 real | Fast | Service logic + persistence | Service with DB ops |
| **Controller** | Services mocked | No | Fast | HTTP layer, validation | Controller endpoints |
| **Full Stack** | Nothing mocked | H2 real | Slowest | Everything | End-to-end flows |

**Recommendation:**
- 50% Unit Tests (Services with mocks)
- 30% Repository Tests (@DataJpaTest)
- 10% Controller Tests (@WebMvcTest)
- 10% Full Stack Tests (@SpringBootTest)

---

## 🎓 Quick Start: First Integration Test

### Step-by-Step

1. Create test class in `src/test/` same package as repository
2. Add `@DataJpaTest` and `@ActiveProfiles("test")`
3. Inject repository with `@Autowired`
4. Write test following AAA pattern
5. Run with Maven: `mvn test -Dtest=YourRepositoryTest`

```java
@DataJpaTest
@ActiveProfiles("test")
public class ProductRepositoryTests {
    
    @Autowired
    private ProductRepository repository;
    
    @Test
    @DisplayName("Should save and retrieve product")
    void testSaveProduct() {
        // ARRANGE
        Product product = new Product();
        product.setName("Test Product");
        product.setActive(true);
        
        // ACT
        Product saved = repository.save(product);
        
        // ASSERT
        assertNotNull(saved.getId());
        assertEquals("Test Product", saved.getName());
    }
}
```

---

## 📚 References

- **AdminProductServiceTests.java** - Unit test example (TESTING.md)
- **Spring Data JPA Documentation**: https://spring.io/projects/spring-data-jpa
- **Spring Boot Testing**: https://spring.io/guides/gs/testing-web/
- **H2 Database**: http://www.h2database.com/

---

## 🎯 Success Criteria

After writing integration tests, you should be able to answer:

- [ ] Do I know when to use @DataJpaTest vs @SpringBootTest?
- [ ] Do I understand what @ActiveProfiles("test") does?
- [ ] Can I test database constraints with integration tests?
- [ ] Can I write controller tests with MockMvc?
- [ ] Do tests verify both result AND database state?
- [ ] Are integration tests separated from unit tests?
- [ ] Do I know the performance implications?

**If yes to all**: Your integration tests are ready! 🎉

---

**Status**: ✅ Complete  
**Last Updated**: April 2026  
**Complements**: TESTING.md (Unit Tests with Mockito)
