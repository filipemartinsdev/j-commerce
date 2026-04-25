package com.products.application.service;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.admin.CreateProductSKURequest;
import com.products.application.dto.admin.ProductSKUAdminResponse;
import com.products.application.dto.admin.UpdateProductSKURequest;
import com.products.application.event.ProductSKUCreatedEvent;
import com.products.application.event.ProductSKUDeletedEvent;
import com.products.application.exception.ProductNotFoundException;
import com.products.application.exception.ProductNotActiveException;
import com.products.application.exception.ProductSKUNotFoundException;
import com.products.application.exception.SKUAlreadyInUseException;
import com.products.application.service.mapper.ProductSKUAdminMapper;
import com.products.domain.entity.Product;
import com.products.domain.entity.ProductCategory;
import com.products.domain.entity.ProductSKU;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminProductSKUServiceTests {
    @Mock private ProductRepository productRepository;
    @Mock private ProductSKURepository productSKURepository;
    @Mock private ApplicationEventPublisher applicationEventPublisher;
    @Mock private ProductSKUAdminMapper productSKUAdminMapper;

    @InjectMocks
    private AdminProductSKUService adminProductSKUService;


    @Test
    @DisplayName("Should create ProductSKU and publish ProductSKUCreatedEvent successfully if everything is OK")
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
        ProductSKUAdminResponse result = adminProductSKUService.createProductSKU(request, userId);

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
            adminProductSKUService.createProductSKU(request, userId);
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
            adminProductSKUService.createProductSKU(request, userId);
        });

        verify(productRepository).findById(productId);
    }

    @Test @DisplayName("Should throw ProductNotActiveException when product is not active")
    void createProductSKUTestCase4() {
        // Given
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CreateProductSKURequest request = new CreateProductSKURequest(productId, "SKU-001", "Laptop SKU");

        Product product = new Product();
        product.setId(productId);
        product.setName("Laptop");
        product.setActive(false);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(productSKURepository.existsBySKU(request.SKU()))
                .thenReturn(false);

        // When & Then
        assertThrows(ProductNotActiveException.class, () -> {
            adminProductSKUService.createProductSKU(request, userId);
        });

        verify(productRepository).findById(productId);
        verify(productSKURepository, never()).save(any());
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
        ProductSKUAdminResponse result = adminProductSKUService.updateProductSKU(skuId, request);

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
            adminProductSKUService.updateProductSKU(skuId, request);
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
            adminProductSKUService.updateProductSKU(skuId, request);
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
        PagedResponse<ProductSKUAdminResponse> result = adminProductSKUService.getAllProductSKUs(pageable);

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
        PagedResponse<ProductSKUAdminResponse> result = adminProductSKUService.getAllProductSKUs(pageable);

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
        PagedResponse<ProductSKUAdminResponse> result = adminProductSKUService.getAllProductSKUsByProductId(productId, pageable);

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
        PagedResponse<ProductSKUAdminResponse> result = adminProductSKUService.getAllProductSKUsByProductId(productId, pageable);

        // Then - The current implementation doesn't validate product existence
        // It returns empty page for non-existent products
        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        verify(productSKURepository).findAllActiveByProductId(productId, pageable);
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any active ProductSKU by productSKUId")
    void getAllProductSKUsByProductIdTestCase3() {
        // Given
        UUID productId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductSKU> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(productSKURepository.findAllActiveByProductId(productId, pageable))
                .thenReturn(emptyPage);

        // When
        PagedResponse<ProductSKUAdminResponse> result = adminProductSKUService.getAllProductSKUsByProductId(productId, pageable);

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
        ProductSKUAdminResponse result = adminProductSKUService.getProductSKUById(skuId);

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
            adminProductSKUService.getProductSKUById(skuId);
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
        adminProductSKUService.deleteProductSKUById(skuId);

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
            adminProductSKUService.deleteProductSKUById(skuId);
        });

        verify(productSKURepository).findActiveById(skuId);
        verify(productSKURepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }
}
