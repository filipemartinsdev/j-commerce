package com.products.application.service;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.ProductCategoryResponse;
import com.products.application.dto.admin.*;
import com.products.application.exception.*;
import com.products.application.factory.PagedResponseFactory;
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
import org.mockito.Mockito;
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
public class ProductManagementServiceTests {
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

    @Mock
    private PagedResponseFactory<ProductAdminResponse> pagedResponseFactory;

    @InjectMocks
    private ProductManagementService productManagementService;

    @Test @DisplayName("Should retrieve all products by category ID successfully")
    void getAllProductsByCategoryIdTestCase1() {
        // Given
        Integer categoryId = 1;
        Pageable pageable = PageRequest.of(0, 10);

        ProductCategory category = new ProductCategory();
        category.setId(categoryId);
        category.setName("Electronics");

        Product product1 = new Product();
        product1.setId(UUID.randomUUID());
        product1.setName("Laptop");
        product1.setDescription("Gaming Laptop");
        product1.setCategory(category);
        product1.setActive(true);

        Product product2 = new Product();
        product2.setId(UUID.randomUUID());
        product2.setName("Mouse");
        product2.setDescription("Wireless Mouse");
        product2.setCategory(category);
        product2.setActive(true);

        Page<Product> page = new PageImpl<>(List.of(product1, product2), pageable, 2);

        ProductAdminResponse response1 = new ProductAdminResponse(
                product1.getId(),
                "Laptop",
                "Gaming Laptop",
                new ProductCategoryResponse(1, "Electronics"),
                Instant.now(),
                Instant.now()
        );
        ProductAdminResponse response2 = new ProductAdminResponse(
                product2.getId(),
                "Mouse",
                "Wireless Mouse",
                new ProductCategoryResponse(1, "Electronics"),
                Instant.now(),
                Instant.now()
        );

        PagedResponse<ProductAdminResponse> expectedResponse = PagedResponse.<ProductAdminResponse>builder()
                .page(0)
                .size(2)
                .isLast(true)
                .totalElements(2L)
                .totalPages(1)
                .content(List.of(response1, response2))
                .build();

        when(productRepository.findAllByCategoryId(categoryId, pageable))
                .thenReturn(page);
        when(pagedResponseFactory.fromPage(any(), any()))
                .thenReturn(expectedResponse);

        // When
        PagedResponse<ProductAdminResponse> result = productManagementService.getAllProductsByCategoryId(categoryId, pageable);

        // Then

        assertEquals(expectedResponse, result);
        verify(productRepository).findAllByCategoryId(categoryId, pageable);
        verify(pagedResponseFactory).fromPage(any(), any());
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists products by category ID")
    void getAllProductsByCategoryIdTestCase2() {
        //        Given
        Integer categoryId = 1;

        PagedResponse<ProductAdminResponse> expectedResponse = PagedResponse.<ProductAdminResponse>builder()
                .content(new ArrayList<>())
                .size(0)
                .page(0)
                .isLast(true)
                .totalPages(1)
                .totalElements(0L)
                .build();

        Mockito.when(productRepository.findAllByCategoryId(any(), any())).thenReturn(Page.empty());
        Mockito.when(pagedResponseFactory.fromPage(any(), any()))
                .thenReturn(expectedResponse);

//        When
        PagedResponse<ProductAdminResponse> response = productManagementService.getAllProductsByCategoryId(categoryId, Pageable.unpaged());

//        Then
        assertEquals(expectedResponse, response);
        verify(pagedResponseFactory).fromPage(any(), any());
    }

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
                new ProductCategoryResponse(1, "Electronics"),
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
        ProductAdminResponse result = productManagementService.createProduct(request);

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
            productManagementService.createProduct(request);
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
                new ProductCategoryResponse(1, "Eletronics"),
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
        ProductAdminResponse result = productManagementService.updateProduct(productId, request);

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
            productManagementService.updateProduct(productId, request);
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
                product1.getId(),
                "Product 1",
                "Description 1",
                new ProductCategoryResponse(1, "Electronics"),
                Instant.now(),
                Instant.now()
        );
        ProductAdminResponse response2 = new ProductAdminResponse(
                product2.getId(),
                "Product 2",
                "Description 2",
                new ProductCategoryResponse(1, "Electronics"),
                Instant.now(),
                Instant.now()
        );

        PagedResponse<ProductAdminResponse> expectedResponse = PagedResponse.<ProductAdminResponse>builder()
                .page(0)
                .size(2)
                .isLast(true)
                .totalElements(2L)
                .totalPages(1)
                .content(List.of(response1, response2))
                .build();

        when(productRepository.findAll(pageable))
                .thenReturn(page);
        when(pagedResponseFactory.fromPage(any(), any()))
                .thenReturn(expectedResponse);

        // When
        PagedResponse<ProductAdminResponse> result = productManagementService.getAllProducts(pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(productRepository).findAll(pageable);
        verify(pagedResponseFactory).fromPage(any(), any());

    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any active product")
    void getAllProductsTestCase2() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        PagedResponse<ProductAdminResponse> expectedResponse = PagedResponse.<ProductAdminResponse>builder()
                .content(new ArrayList<>())
                .size(0)
                .page(0)
                .isLast(true)
                .totalPages(1)
                .totalElements(0L)
                .build();

        when(productRepository.findAll(pageable))
                .thenReturn(emptyPage);
        when(pagedResponseFactory.fromPage(any(), any()))
                .thenReturn(expectedResponse);

        // When
        PagedResponse<ProductAdminResponse> result = productManagementService.getAllProducts(pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(productRepository).findAll(pageable);
        verify(pagedResponseFactory).fromPage(any(), any());
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
                productId,
                "Laptop",
                "Gaming Laptop",
                new ProductCategoryResponse(1, "Electronics"),
                Instant.now(),
                Instant.now()
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(productAdminMapper.toResponse(product))
                .thenReturn(response);

        // When
        ProductAdminResponse result = productManagementService.getProductById(productId);

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
            productManagementService.getProductById(productId);
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
        productManagementService.deleteProductById(productId);

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
            productManagementService.deleteProductById(productId);
        });

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
    }

    @Test @DisplayName("Should throw CantDeleteProductException when product has active SKU")
    void deleteProductByIdTestCase3() {
        // Given
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setId(productId);
        product.setName("Laptop");
        product.setActive(true);

        ProductSKU activeSKU = new ProductSKU();
        activeSKU.setIsActive(true);
        activeSKU.setName("SKU-001");
        product.setSKUs(new ArrayList<>(List.of(activeSKU)));

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        // When & Then
        assertThrows(CantDeleteProductException.class, () -> {
            productManagementService.deleteProductById(productId);
        });

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
    }

    @Test @DisplayName("Should delete product when all SKUs are inactive")
    void deleteProductByIdTestCase4() {
        // Given
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setId(productId);
        product.setName("Laptop");
        product.setActive(true);

        ProductSKU inactiveSKU = new ProductSKU();
        inactiveSKU.setIsActive(false);
        inactiveSKU.setName("SKU-001");
        product.setSKUs(new ArrayList<>(List.of(inactiveSKU)));

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        // When
        productManagementService.deleteProductById(productId);

        // Then
        assertFalse(product.isActive());
        verify(productRepository).save(product);
    }

}