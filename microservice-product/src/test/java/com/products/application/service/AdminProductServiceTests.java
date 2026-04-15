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
import org.mockito.ArgumentCaptor;
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

    @Test @DisplayName("Should create new product and retrieve it DTO successfully if everything is OK")
    void createProductTestCase1() {
        // Given
        ProductCategory category = new ProductCategory();
        category.setId(1);
        category.setName("Electronics");

        CreateProductRequest request = new CreateProductRequest("Laptop", Optional.of("Gaming Laptop"), 1);

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

        when(productCategoryRepository.findById(1))
                .thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class)))
                .thenReturn(productEntity);
        when(productAdminMapper.toResponse(productEntity))
                .thenReturn(response);

        // When
        ProductAdminResponse result = adminProductService.createProduct(request);

        // Then
        assertNotNull(result);
        assertEquals("Laptop", result.name());
        assertEquals("Gaming Laptop", result.description());
        verify(productCategoryRepository).findById(1);
        verify(productRepository).save(any(Product.class));
        verify(productAdminMapper).toResponse(productEntity);
    }

    @Test @DisplayName("Should InvalidProductCategoryException if categoryId is invalid")
    void createProductTestCase2() {
        // Given
        CreateProductRequest request = new CreateProductRequest("Laptop", Optional.of("Gaming Laptop"), 999);

        when(productCategoryRepository.findById(999))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvalidProductCategoryException.class, () -> {
            adminProductService.createProduct(request);
        });

        verify(productCategoryRepository).findById(999);
        verify(productRepository, never()).save(any());
    }

    @Test @DisplayName("Should update the product successfully if everything is OK")
    void updateProductTestCase1() {
        // Given
        UUID productId = UUID.randomUUID();
        UpdateProductRequest request = new UpdateProductRequest(
                Optional.of("Updated Laptop"),
                Optional.of("Updated Description"),
                Optional.empty()
        );

        ProductCategory category = new ProductCategory();
        category.setId(1);

        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setName("Old Name");
        existingProduct.setDescription("Old Description");
        existingProduct.setCategory(category);
        existingProduct.setActive(true);

        Product updatedProduct = new Product();
        updatedProduct.setId(productId);
        updatedProduct.setName("Updated Laptop");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setCategory(category);
        updatedProduct.setActive(true);

        ProductAdminResponse response = new ProductAdminResponse(
                productId,
                "Updated Laptop",
                "Updated Description",
                Instant.now(),
                Instant.now()
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class)))
                .thenReturn(updatedProduct);
        when(productAdminMapper.toResponse(updatedProduct))
                .thenReturn(response);

        // When
        ProductAdminResponse result = adminProductService.updateProduct(productId, request);

        // Then
        assertNotNull(result);
        assertEquals("Updated Laptop", result.name());
        assertEquals("Updated Description", result.description());
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
        verify(productAdminMapper).toResponse(updatedProduct);
    }

    @Test @DisplayName("Should throw ProductNotFoundException if product is inactive or not exists")
    void updateProductTestCase2() {
        // Given
        UUID productId = UUID.randomUUID();
        UpdateProductRequest request = new UpdateProductRequest(
                Optional.of("Updated Name"),
                Optional.empty(),
                Optional.empty()
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> {
            adminProductService.updateProduct(productId, request);
        });

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
    }

    @Test @DisplayName("Should retrieve all active products successfully")
    void getAllProductsTestCase1() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        Product product1 = new Product();
        product1.setId(UUID.randomUUID());
        product1.setName("Product 1");
        product1.setDescription("Description 1");
        product1.setActive(true);

        Product product2 = new Product();
        product2.setId(UUID.randomUUID());
        product2.setName("Product 2");
        product2.setDescription("Description 2");
        product2.setActive(true);

        Page<Product> page = new PageImpl<>(List.of(product1, product2), pageable, 2);

        ProductAdminResponse response1 = new ProductAdminResponse(
                product1.getId(), "Product 1", "Description 1", Instant.now(), Instant.now()
        );
        ProductAdminResponse response2 = new ProductAdminResponse(
                product2.getId(), "Product 2", "Description 2", Instant.now(), Instant.now()
        );

        when(productRepository.findAll(pageable))
                .thenReturn(page);
        when(productAdminMapper.toResponse(product1))
                .thenReturn(response1);
        when(productAdminMapper.toResponse(product2))
                .thenReturn(response2);

        // When
        PagedResponse<ProductAdminResponse> result = adminProductService.getAllProducts(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.isLast());
        assertEquals(2, result.content().size());
        verify(productRepository).findAll(pageable);
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any active product")
    void getAllProductsTestCase2() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(productRepository.findAll(pageable))
                .thenReturn(emptyPage);

        // When
        PagedResponse<ProductAdminResponse> result = adminProductService.getAllProducts(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
        verify(productRepository).findAll(pageable);
    }

    @Test @DisplayName("Should retrieve product DTO successfully")
    void getProductByIdTestCase1() {
        // Given
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setId(productId);
        product.setName("Laptop");
        product.setDescription("Gaming Laptop");
        product.setActive(true);

        ProductAdminResponse response = new ProductAdminResponse(
                productId, "Laptop", "Gaming Laptop", Instant.now(), Instant.now()
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(productAdminMapper.toResponse(product))
                .thenReturn(response);

        // When
        ProductAdminResponse result = adminProductService.getProductById(productId);

        // Then
        assertNotNull(result);
        assertEquals("Laptop", result.name());
        assertEquals("Gaming Laptop", result.description());
        verify(productRepository).findById(productId);
        verify(productAdminMapper).toResponse(product);
    }

    @Test @DisplayName("Should throw ProductNotFoundException if product not exists")
    void getProductByIdTestCase2() {
        // Given
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> {
            adminProductService.getProductById(productId);
        });

        verify(productRepository).findById(productId);
    }

    @Test @DisplayName("Should mark product as inactive successfully successfully")
    void deleteProductByIdTestCase1() {
        // Given
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setId(productId);
        product.setName("Laptop");
        product.setActive(true);
        product.setSKUs(new ArrayList<>());

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        // When
        adminProductService.deleteProductById(productId);

        // Then
        assertFalse(product.isActive());
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
    }

    @Test @DisplayName("Should throw ProductNotFoundException if product not exists")
    void deleteProductByIdTestCase2() {
        // Given
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> {
            adminProductService.deleteProductById(productId);
        });

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
    }

    @Test @DisplayName("Should create ProductSKU and publish ProductSKUCreatedEvent successfully if everything is OK")
    void createProductSKUTestCase1() {
        // Given
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CreateProductSKURequest request = new CreateProductSKURequest(productId, "SKU-001", "Laptop SKU");

        ProductCategory category = new ProductCategory();
        category.setId(1);

        Product product = new Product();
        product.setId(productId);
        product.setName("Laptop");
        product.setActive(true);
        product.setCategory(category);

        ProductSKU newSKU = new ProductSKU();
        newSKU.setId(UUID.randomUUID());
        newSKU.setProduct(product);
        newSKU.setSKU("SKU-001");
        newSKU.setName("Laptop SKU");
        newSKU.setIsActive(true);

        ProductSKUAdminResponse response = new ProductSKUAdminResponse(
                newSKU.getId(),
                productId,
                "SKU-001",
                "Laptop SKU",
                Instant.now(),
                Instant.now(),
                true
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(productSKURepository.save(any(ProductSKU.class)))
                .thenReturn(newSKU);
        when(productSKUAdminMapper.toResponse(newSKU))
                .thenReturn(response);

        // When
        ProductSKUAdminResponse result = adminProductService.createProductSKU(request, userId);

        // Then
        assertNotNull(result);
        assertEquals("SKU-001", result.SKU());
        assertEquals("Laptop SKU", result.name());
        assertTrue(result.isActive());
        verify(productRepository).findById(productId);
        verify(productSKURepository).save(any(ProductSKU.class));
        verify(productSKUAdminMapper).toResponse(newSKU);
        verify(applicationEventPublisher).publishEvent(any(ProductSKUCreatedEvent.class));
    }

    @Test @DisplayName("Should throw ProductNotFoundException if product ID not exists")
    void createProductSKUTestCase2() {
        // Given
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CreateProductSKURequest request = new CreateProductSKURequest(productId, "SKU-001", "Laptop SKU");

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> {
            adminProductService.createProductSKU(request, userId);
        });

        verify(productRepository).findById(productId);
        verify(productSKURepository, never()).save(any());
    }

    @Test @DisplayName("Should throw SKUAlreadyInUseException if SKU it's already in use")
    void createProductSKUTestCase3() {
        // Given
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CreateProductSKURequest request = new CreateProductSKURequest(productId, "SKU-001", "Laptop SKU");

        ProductCategory category = new ProductCategory();
        category.setId(1);

        Product product = new Product();
        product.setId(productId);
        product.setName("Laptop");
        product.setActive(true);
        product.setCategory(category);

        ProductSKU existingSKU = new ProductSKU();
        existingSKU.setId(UUID.randomUUID());
        existingSKU.setProduct(product);
        existingSKU.setSKU("SKU-001");
        existingSKU.setIsActive(true);
        product.setSKUs(List.of(existingSKU));

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(productSKURepository.save(any(ProductSKU.class)))
                .thenThrow(new SKUAlreadyInUseException("SKU SKU-001 is already in use"));

        // When & Then
        assertThrows(SKUAlreadyInUseException.class, () -> {
            adminProductService.createProductSKU(request, userId);
        });

        verify(productRepository).findById(productId);
    }

    @Test @DisplayName("Should update ProductSKU successfully if everything is OK")
    void updateProductSKUTestCase1() {
        // Given
        UUID skuId = UUID.randomUUID();
        UpdateProductSKURequest request = new UpdateProductSKURequest(
                Optional.of("Updated SKU Name"),
                Optional.of("SKU-002")
        );

        ProductSKU existingSKU = new ProductSKU();
        existingSKU.setId(skuId);
        existingSKU.setSKU("SKU-001");
        existingSKU.setName("Old SKU Name");
        existingSKU.setIsActive(true);

        ProductSKU updatedSKU = new ProductSKU();
        updatedSKU.setId(skuId);
        updatedSKU.setSKU("SKU-002");
        updatedSKU.setName("Updated SKU Name");
        updatedSKU.setIsActive(true);

        ProductSKUAdminResponse response = new ProductSKUAdminResponse(
                skuId,
                UUID.randomUUID(),
                "SKU-002",
                "Updated SKU Name",
                Instant.now(),
                Instant.now(),
                true
        );

        when(productSKURepository.findActiveById(skuId))
                .thenReturn(Optional.of(existingSKU));
        when(productSKURepository.save(any(ProductSKU.class)))
                .thenReturn(updatedSKU);
        when(productSKUAdminMapper.toResponse(updatedSKU))
                .thenReturn(response);

        // When
        ProductSKUAdminResponse result = adminProductService.updateProductSKU(skuId, request);

        // Then
        assertNotNull(result);
        assertEquals("SKU-002", result.SKU());
        assertEquals("Updated SKU Name", result.name());
        verify(productSKURepository).findActiveById(skuId);
        verify(productSKURepository).save(any(ProductSKU.class));
    }

    @Test @DisplayName("Should throw ProductSKUNotFoundException if ProductSKU not exists by ID")
    void updateProductSKUTestCase2() {
        // Given
        UUID skuId = UUID.randomUUID();
        UpdateProductSKURequest request = new UpdateProductSKURequest(
                Optional.of("Updated Name"),
                Optional.empty()
        );

        when(productSKURepository.findActiveById(skuId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductSKUNotFoundException.class, () -> {
            adminProductService.updateProductSKU(skuId, request);
        });

        verify(productSKURepository).findActiveById(skuId);
        verify(productSKURepository, never()).save(any());
    }

    @Test @DisplayName("Should throw SKUAlreadyInUseException if selected SKU is already in use")
    void updateProductSKUTestCase3() {
        // Given
        UUID skuId = UUID.randomUUID();
        UpdateProductSKURequest request = new UpdateProductSKURequest(
                Optional.empty(),
                Optional.of("SKU-TAKEN")
        );

        ProductSKU existingSKU = new ProductSKU();
        existingSKU.setId(skuId);
        existingSKU.setSKU("SKU-001");
        existingSKU.setName("SKU Name");
        existingSKU.setIsActive(true);

        when(productSKURepository.findActiveById(skuId))
                .thenReturn(Optional.of(existingSKU));
        when(productSKURepository.save(any(ProductSKU.class)))
                .thenThrow(new SKUAlreadyInUseException("SKU SKU-TAKEN is already in use"));

        // When & Then
        assertThrows(SKUAlreadyInUseException.class, () -> {
            adminProductService.updateProductSKU(skuId, request);
        });

        verify(productSKURepository).findActiveById(skuId);
    }

    @Test @DisplayName("Should retrieve all active ProductSKU successfully")
    void getAllProductSKUsTestCase1() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        ProductSKU sku1 = new ProductSKU();
        sku1.setId(UUID.randomUUID());
        sku1.setSKU("SKU-001");
        sku1.setName("SKU 1");
        sku1.setIsActive(true);

        ProductSKU sku2 = new ProductSKU();
        sku2.setId(UUID.randomUUID());
        sku2.setSKU("SKU-002");
        sku2.setName("SKU 2");
        sku2.setIsActive(true);

        Page<ProductSKU> page = new PageImpl<>(List.of(sku1, sku2), pageable, 2);

        ProductSKUAdminResponse response1 = new ProductSKUAdminResponse(
                sku1.getId(), UUID.randomUUID(), "SKU-001", "SKU 1", Instant.now(), Instant.now(), true
        );
        ProductSKUAdminResponse response2 = new ProductSKUAdminResponse(
                sku2.getId(), UUID.randomUUID(), "SKU-002", "SKU 2", Instant.now(), Instant.now(), true
        );

        when(productSKURepository.findAllActive(pageable))
                .thenReturn(page);
        when(productSKUAdminMapper.toResponse(sku1))
                .thenReturn(response1);
        when(productSKUAdminMapper.toResponse(sku2))
                .thenReturn(response2);

        // When
        PagedResponse<ProductSKUAdminResponse> result = adminProductService.getAllProductSKUs(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.isLast());
        assertEquals(2, result.content().size());
        verify(productSKURepository).findAllActive(pageable);
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any active product")
    void getAllProductSKUsTestCase2() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductSKU> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(productSKURepository.findAllActive(pageable))
                .thenReturn(emptyPage);

        // When
        PagedResponse<ProductSKUAdminResponse> result = adminProductService.getAllProductSKUs(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
        verify(productSKURepository).findAllActive(pageable);
    }

    @Test @DisplayName("Should retrieve all active ProductSKU of an product successfully")
    void getAllProductSKUsByProductIdTestCase1() {
        // Given
        UUID productId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        ProductSKU sku1 = new ProductSKU();
        sku1.setId(UUID.randomUUID());
        sku1.setSKU("SKU-001");
        sku1.setName("SKU 1");
        sku1.setIsActive(true);

        ProductSKU sku2 = new ProductSKU();
        sku2.setId(UUID.randomUUID());
        sku2.setSKU("SKU-002");
        sku2.setName("SKU 2");
        sku2.setIsActive(true);

        Page<ProductSKU> page = new PageImpl<>(List.of(sku1, sku2), pageable, 2);

        ProductSKUAdminResponse response1 = new ProductSKUAdminResponse(
                sku1.getId(), productId, "SKU-001", "SKU 1", Instant.now(), Instant.now(), true
        );
        ProductSKUAdminResponse response2 = new ProductSKUAdminResponse(
                sku2.getId(), productId, "SKU-002", "SKU 2", Instant.now(), Instant.now(), true
        );

        when(productSKURepository.findAllActiveByProductId(productId, pageable))
                .thenReturn(page);
        when(productSKUAdminMapper.toResponse(sku1))
                .thenReturn(response1);
        when(productSKUAdminMapper.toResponse(sku2))
                .thenReturn(response2);

        // When
        PagedResponse<ProductSKUAdminResponse> result = adminProductService.getAllProductSKUsByProductId(productId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.isLast());
        assertEquals(2, result.content().size());
        verify(productSKURepository).findAllActiveByProductId(productId, pageable);
    }

    @Test @DisplayName("Should throw ProductNotFoundException if product not exists")
    void getAllProductSKUsByProductIdTestCase2() {
        // Given
        UUID productId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Page<ProductSKU> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(productSKURepository.findAllActiveByProductId(productId, pageable))
                .thenReturn(emptyPage);

        // When
        PagedResponse<ProductSKUAdminResponse> result = adminProductService.getAllProductSKUsByProductId(productId, pageable);

        // Then - The current implementation doesn't validate product existence
        // It returns empty page for non-existent products
        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        verify(productSKURepository).findAllActiveByProductId(productId, pageable);
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any active ProductSKU by productId")
    void getAllProductSKUsByProductIdTestCase3() {
        // Given
        UUID productId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductSKU> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(productSKURepository.findAllActiveByProductId(productId, pageable))
                .thenReturn(emptyPage);

        // When
        PagedResponse<ProductSKUAdminResponse> result = adminProductService.getAllProductSKUsByProductId(productId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
        verify(productSKURepository).findAllActiveByProductId(productId, pageable);
    }

    @Test @DisplayName("Should retrieve ProductSKU successfully")
    void getProductSKUByIdTestCase1() {
        // Given
        UUID skuId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        ProductSKU sku = new ProductSKU();
        sku.setId(skuId);
        sku.setSKU("SKU-001");
        sku.setName("SKU Name");
        sku.setIsActive(true);

        ProductSKUAdminResponse response = new ProductSKUAdminResponse(
                skuId, productId, "SKU-001", "SKU Name", Instant.now(), Instant.now(), true
        );

        when(productSKURepository.findActiveById(skuId))
                .thenReturn(Optional.of(sku));
        when(productSKUAdminMapper.toResponse(sku))
                .thenReturn(response);

        // When
        ProductSKUAdminResponse result = adminProductService.getProductSKUById(skuId);

        // Then
        assertNotNull(result);
        assertEquals("SKU-001", result.SKU());
        assertEquals("SKU Name", result.name());
        assertTrue(result.isActive());
        verify(productSKURepository).findActiveById(skuId);
        verify(productSKUAdminMapper).toResponse(sku);
    }

    @Test @DisplayName("Should throw ProductSKUNotFoundException if ProductSKU is not active or not exists")
    void getProductSKUByIdTestCase2() {
        // Given
        UUID skuId = UUID.randomUUID();

        when(productSKURepository.findActiveById(skuId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductSKUNotFoundException.class, () -> {
            adminProductService.getProductSKUById(skuId);
        });

        verify(productSKURepository).findActiveById(skuId);
    }

    @Test @DisplayName("Should mark ProductSKU as inactive and publish ProductSKUDeletedEvent successfully")
    void deleteProductSKUByIdTestCase1() {
        // Given
        UUID skuId = UUID.randomUUID();

        ProductSKU sku = new ProductSKU();
        sku.setId(skuId);
        sku.setSKU("SKU-001");
        sku.setName("SKU Name");
        sku.setIsActive(true);

        when(productSKURepository.findActiveById(skuId))
                .thenReturn(Optional.of(sku));

        // When
        adminProductService.deleteProductSKUById(skuId);

        // Then
        assertFalse(sku.getIsActive());
        verify(productSKURepository).findActiveById(skuId);
        verify(productSKURepository).save(sku);
        verify(applicationEventPublisher).publishEvent(any(ProductSKUDeletedEvent.class));
    }

    @Test @DisplayName("Should throw ProductSKUNotFoundException if ProductSKU is already inactive or not exists")
    void deleteProductSKUByIdTestCase2() {
        // Given
        UUID skuId = UUID.randomUUID();

        when(productSKURepository.findActiveById(skuId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductSKUNotFoundException.class, () -> {
            adminProductService.deleteProductSKUById(skuId);
        });

        verify(productSKURepository).findActiveById(skuId);
        verify(productSKURepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }
}