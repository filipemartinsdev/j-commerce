# Reference Files

## Test Examples (microservice-product)

Structure to replicate when creating unit tests:

1. `microservice-product/src/test/java/com/products/application/service/AdminProductServiceTests.java`
2. `microservice-product/src/test/java/com/products/application/service/AdminProductSKUServiceTests.java`
3. `microservice-product/src/test/java/com/products/application/service/AdminProductPriceServiceTests.java`
4. `microservice-product/src/test/java/com/products/application/service/AdminProductStockServiceTests.java`
5. `microservice-product/src/test/java/com/products/application/service/ProductCatalogueServiceTests.java`
6. `microservice-product/src/test/java/com/products/application/service/WishlistServiceTests.java`
7. `microservice-product/src/test/java/com/products/application/service/ShoppingCartServiceTests.java`

## Test Examples (microservice-identity)

1. `microservice-identity/src/test/java/com/identity/security/application/service/AuthServiceTest.java`

## Documentation (Root Level)

1. `README.md` - Main project documentation
2. `microservice-identity/README.md` - Identity service
3. `microservice-product/README.md` - Product service
4. `microservice-order/README.md` - Order service
5. `microservice-payment/README.md` - Payment service
6. `microservice-notification/README.md` - Notification service

## Entity Patterns

- `microservice-identity/src/main/java/com/identity/security/domain/entity/UserCredentials.java`
- `microservice-identity/src/main/java/com/identity/security/domain/entity/Role.java`
- `microservice-product/src/main/java/com/products/domain/entity/Product.java`
- `microservice-product/src/main/java/com/products/domain/entity/ProductSKU.java`

## Service Patterns

- `microservice-identity/src/main/java/com/identity/security/application/service/AuthService.java`
- `microservice-product/src/main/java/com/products/application/service/AdminProductService.java`

## DTO Patterns

- `microservice-product/src/main/java/com/products/application/dto/admin/CreateProductRequest.java`
- `microservice-product/src/main/java/com/products/application/dto/admin/ProductAdminResponse.java`
- `microservice-identity/src/main/java/com/identity/security/application/dto/RegisterRequest.java`

## Exception Patterns

- `microservice-identity/src/main/java/com/identity/security/application/exception/UserNotFoundException.java`
- `microservice-product/src/main/java/com/products/application/exception/ProductNotFoundException.java`

## Global Exception Handler

- `microservice-identity/src/main/java/com/identity/common/handler/GlobalExceptionHandler.java`

## Mapper Pattern

- `microservice-product/src/main/java/com/products/application/service/mapper/ProductAdminMapper.java`

## Migration Patterns

- `microservice-identity/src/main/resources/db/migration/V1__init.sql`
- `microservice-product/src/main/resources/db/migration/V1__init.sql`

## Configuration

- `microservice-identity/src/main/java/com/identity/config/SecurityConfig.java`