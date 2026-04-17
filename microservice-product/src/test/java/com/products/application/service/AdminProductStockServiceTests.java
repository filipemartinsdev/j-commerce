package com.products.application.service;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.admin.CreateStockEntryRequest;
import com.products.application.dto.admin.ProductStockResponse;
import com.products.application.dto.admin.StockMovementResponse;
import com.products.application.dto.admin.StockMovementTypeResponse;
import com.products.application.exception.ProductNotFoundException;
import com.products.application.exception.ProductSKUNotFoundException;
import com.products.application.exception.ProductStockNotFoundException;
import com.products.application.service.mapper.ProductStockMapper;
import com.products.application.service.mapper.StockMovementMapper;
import com.products.domain.entity.*;
import com.products.infra.persistence.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminProductStockServiceTests {
    @Mock private ProductStockRepository productStockRepository;
    @Mock private ProductStockMapper productStockMapper;
    @Mock private StockMovementTypeRepository stockMovementTypeRepository;
    @Mock private StockMovementRepository stockMovementRepository;
    @Mock private StockMovementMapper stockMovementMapper;
    @Mock private ProductSKURepository productSKURepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private AdminProductStockService adminProductStockService;

    @Test @DisplayName("Should retrieve all active ProductStock successfully")
    void getAllTestCase1() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        ProductStock stock1 = new ProductStock();
        stock1.setId(UUID.randomUUID());
        stock1.setUnits(50);
        stock1.setIsActive(true);

        ProductStock stock2 = new ProductStock();
        stock2.setId(UUID.randomUUID());
        stock2.setUnits(100);
        stock2.setIsActive(true);

        Page<ProductStock> page = new PageImpl<>(List.of(stock1, stock2), pageable, 2);

        ProductStockResponse response1 = new ProductStockResponse(
                stock1.getId(), UUID.randomUUID(), UUID.randomUUID(), "Product1", "SKU1", 50, Instant.now()
        );
        ProductStockResponse response2 = new ProductStockResponse(
                stock2.getId(), UUID.randomUUID(), UUID.randomUUID(), "Product2", "SKU2", 100, Instant.now()
        );

        when(productStockRepository.findAllActive(pageable)).thenReturn(page);
        when(productStockMapper.toResponse(stock1)).thenReturn(response1);
        when(productStockMapper.toResponse(stock2)).thenReturn(response2);

        // When
        PagedResponse<ProductStockResponse> result = adminProductStockService.getAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.isLast());
        assertEquals(2, result.content().size());
        verify(productStockRepository).findAllActive(pageable);
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any active ProductStock")
    void getAllTestCase2() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductStock> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(productStockRepository.findAllActive(pageable)).thenReturn(emptyPage);

        // When
        PagedResponse<ProductStockResponse> result = adminProductStockService.getAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
        verify(productStockRepository).findAllActive(pageable);
    }

    @Test @DisplayName("Should retrieve all ProductStock by productSKUId successfully")
    void getAllByProductIdTestCase1() {
        // Given
        UUID productId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        ProductStock stock1 = new ProductStock();
        stock1.setId(UUID.randomUUID());
        stock1.setUnits(50);
        stock1.setIsActive(true);

        Page<ProductStock> page = new PageImpl<>(List.of(stock1), pageable, 1);

        ProductStockResponse response1 = new ProductStockResponse(
                stock1.getId(), productId, UUID.randomUUID(), "Product1", "SKU1", 50, Instant.now()
        );

        when(productRepository.existsById(productId)).thenReturn(true);
        when(productStockRepository.findAllActiveByProductId(productId, pageable)).thenReturn(page);
        when(productStockMapper.toResponse(stock1)).thenReturn(response1);

        // When
        PagedResponse<ProductStockResponse> result = adminProductStockService.getAllByProductId(productId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals(1, result.content().size());
        verify(productRepository).existsById(productId);
        verify(productStockRepository).findAllActiveByProductId(productId, pageable);
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any active ProductStock by productSKUId")
    void getAllByProductIdTestCase2() {
        // Given
        UUID productId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductStock> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(productRepository.existsById(productId)).thenReturn(true);
        when(productStockRepository.findAllActiveByProductId(productId, pageable)).thenReturn(emptyPage);

        // When
        PagedResponse<ProductStockResponse> result = adminProductStockService.getAllByProductId(productId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
        verify(productRepository).existsById(productId);
        verify(productStockRepository).findAllActiveByProductId(productId, pageable);
    }

    @Test @DisplayName("Should throw ProductNotFoundException if product is not active or not exists by ID")
    void getAllByProductIdTestCase3() {
        // Given
        UUID productId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.existsById(productId)).thenReturn(false);

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> {
            adminProductStockService.getAllByProductId(productId, pageable);
        });

        verify(productRepository).existsById(productId);
        verify(productStockRepository, never()).findAllActiveByProductId(any(), any());
    }

    @Test @DisplayName("Should retrieve ProductStock by ID successfully")
    void getByIdTestCase1() {
        // Given
        UUID stockId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        ProductStock stock = new ProductStock();
        stock.setId(stockId);
        stock.setUnits(50);
        stock.setIsActive(true);

        ProductStockResponse response = new ProductStockResponse(
                stockId, productId, UUID.randomUUID(), "Product1", "SKU1", 50, Instant.now()
        );

        when(productStockRepository.findActiveById(stockId)).thenReturn(Optional.of(stock));
        when(productStockMapper.toResponse(stock)).thenReturn(response);

        // When
        ProductStockResponse result = adminProductStockService.getById(stockId);

        // Then
        assertNotNull(result);
        assertEquals(stockId, result.id());
        verify(productStockRepository).findActiveById(stockId);
        verify(productStockMapper).toResponse(stock);
    }

    @Test @DisplayName("Should throw ProductStockNotFoundException if ProductStock is not active or not exists by ID")
    void getByIdTestCase2() {
        // Given
        UUID stockId = UUID.randomUUID();

        when(productStockRepository.findActiveById(stockId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductStockNotFoundException.class, () -> {
            adminProductStockService.getById(stockId);
        });

        verify(productStockRepository).findActiveById(stockId);
    }

    @Test @DisplayName("Should create stock entry successfully if everything is OK")
    void createStockEntryTestCase1() {
        // Given
        UUID productSKUId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Integer units = 50;

        ProductStock existingStock = new ProductStock();
        existingStock.setId(UUID.randomUUID());
        existingStock.setUnits(100);
        existingStock.setIsActive(true);

        ProductSKU productSKU = new ProductSKU();
        productSKU.setId(productSKUId);

        StockMovementType entryType = new StockMovementType();
        entryType.setId(StockMovementType.Value.ENTRY.getId());

        CreateStockEntryRequest request = new CreateStockEntryRequest(productSKUId, units);

        when(productStockRepository.findByProductSKU_id(productSKUId)).thenReturn(Optional.of(existingStock));
        when(productSKURepository.findById(productSKUId)).thenReturn(Optional.of(productSKU));
        when(stockMovementTypeRepository.getReferenceById(StockMovementType.Value.ENTRY.getId()))
                .thenReturn(entryType);

        // When
        adminProductStockService.createStockEntry(request, userId);

        // Then
        ArgumentCaptor<ProductStock> stockCaptor = ArgumentCaptor.forClass(ProductStock.class);
        verify(productStockRepository).save(stockCaptor.capture());
        assertEquals(150, stockCaptor.getValue().getUnits());

        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movementCaptor.capture());
        assertEquals(units, movementCaptor.getValue().getUnits());
        assertEquals(userId, movementCaptor.getValue().getCreatedBy());
    }

    @Test @DisplayName("Should throw ProductSKUNotFoundException if ProductSKU not exists")
    void createStockEntryTestCase2() {
        // Given
        UUID productSKUId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Integer units = 50;

        ProductStock existingStock = new ProductStock();
        existingStock.setUnits(100);

        CreateStockEntryRequest request = new CreateStockEntryRequest(productSKUId, units);

        when(productStockRepository.findByProductSKU_id(productSKUId)).thenReturn(Optional.of(existingStock));
        when(productSKURepository.findById(productSKUId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductSKUNotFoundException.class, () -> {
            adminProductStockService.createStockEntry(request, userId);
        });

        verify(productSKURepository).findById(productSKUId);
        verify(stockMovementRepository, never()).save(any());
    }

    @Test @DisplayName("Should throw ProductStockNotFoundException if ProductStock is not active or not exists by productSKUId")
    void createStockEntryTestCase3() {
        // Given
        UUID productSKUId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Integer units = 50;

        CreateStockEntryRequest request = new CreateStockEntryRequest(productSKUId, units);

        when(productStockRepository.findByProductSKU_id(productSKUId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductStockNotFoundException.class, () -> {
            adminProductStockService.createStockEntry(request, userId);
        });

        verify(productStockRepository).findByProductSKU_id(productSKUId);
        verify(productSKURepository, never()).findById(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test @DisplayName("Should create empty stock record for productSKUId")
    void createStockToSKUTestCase1() {
        // Given
        UUID productSKUId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ProductSKU productSKU = new ProductSKU();
        productSKU.setId(productSKUId);

        // When
        adminProductStockService.createStockToSKU(productSKU, userId);

        // Then
        ArgumentCaptor<ProductStock> captor = ArgumentCaptor.forClass(ProductStock.class);
        verify(productStockRepository).save(captor.capture());

        ProductStock savedStock = captor.getValue();
        assertEquals(productSKU, savedStock.getProductSKU());
        assertEquals(userId, savedStock.getCreatedBy());
        assertTrue(savedStock.getIsActive());
    }

    @Test @DisplayName("Should retrieve all StockMovement successfully")
    void getAllMovementsTestCase1() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        StockMovement movement1 = new StockMovement();
        movement1.setId(UUID.randomUUID());
        movement1.setUnits(50);

        StockMovement movement2 = new StockMovement();
        movement2.setId(UUID.randomUUID());
        movement2.setUnits(100);

        Page<StockMovement> page = new PageImpl<>(List.of(movement1, movement2), pageable, 2);

        StockMovementResponse response1 = new StockMovementResponse(
                movement1.getId(), UUID.randomUUID(), "SKU1", 50,
                new StockMovementTypeResponse(1, "ENTRY"), Instant.now(), UUID.randomUUID()
        );
        StockMovementResponse response2 = new StockMovementResponse(
                movement2.getId(), UUID.randomUUID(), "SKU2", 100,
                new StockMovementTypeResponse(2, "EXIT"), Instant.now(), UUID.randomUUID()
        );

        when(stockMovementRepository.findAll(pageable)).thenReturn(page);
        when(stockMovementMapper.toResponse(movement1)).thenReturn(response1);
        when(stockMovementMapper.toResponse(movement2)).thenReturn(response2);

        // When
        PagedResponse<StockMovementResponse> result = adminProductStockService.getAllMovements(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.totalElements());
        assertEquals(2, result.content().size());
        verify(stockMovementRepository).findAll(pageable);
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any StockMovement")
    void getAllMovementsTestCase2() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<StockMovement> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(stockMovementRepository.findAll(pageable)).thenReturn(emptyPage);

        // When
        PagedResponse<StockMovementResponse> result = adminProductStockService.getAllMovements(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
        verify(stockMovementRepository).findAll(pageable);
    }

    @Test @DisplayName("Should retrieve all StockMovent by productSKUId successfully")
    void getAllMovementsByProductSKUIdTestCase1() {
        // Given
        UUID productSKUId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        StockMovement movement1 = new StockMovement();
        movement1.setId(UUID.randomUUID());
        movement1.setUnits(50);

        Page<StockMovement> page = new PageImpl<>(List.of(movement1), pageable, 1);

        StockMovementResponse response1 = new StockMovementResponse(
                movement1.getId(), productSKUId, "SKU1", 50,
                new StockMovementTypeResponse(1, "ENTRY"), Instant.now(), UUID.randomUUID()
        );

        when(stockMovementRepository.findAllByProductSKU_id(productSKUId, pageable)).thenReturn(page);
        when(stockMovementMapper.toResponse(movement1)).thenReturn(response1);

        // When
        PagedResponse<StockMovementResponse> result = adminProductStockService.getAllMovementsByProductSKUId(productSKUId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals(1, result.content().size());
        verify(stockMovementRepository).findAllByProductSKU_id(productSKUId, pageable);
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any StockMovement by productSKUId")
    void getAllMovementsByProductSKUIdTestCase2() {
        // Given
        UUID productSKUId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<StockMovement> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(stockMovementRepository.findAllByProductSKU_id(productSKUId, pageable)).thenReturn(emptyPage);

        // When
        PagedResponse<StockMovementResponse> result = adminProductStockService.getAllMovementsByProductSKUId(productSKUId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
        verify(stockMovementRepository).findAllByProductSKU_id(productSKUId, pageable);
    }

    @Test @DisplayName("Should mark ProductStock as inactive by productSKUId successfully")
    void deleteByProductSKUIdTestCase1() {
        // Given
        UUID productSKUId = UUID.randomUUID();

        ProductStock stock = new ProductStock();
        stock.setId(UUID.randomUUID());
        stock.setUnits(50);
        stock.setIsActive(true);

        when(productStockRepository.findByProductSKU_id(productSKUId)).thenReturn(Optional.of(stock));

        // When
        adminProductStockService.deleteByProductSKUId(productSKUId);

        // Then
        ArgumentCaptor<ProductStock> captor = ArgumentCaptor.forClass(ProductStock.class);
        verify(productStockRepository).save(captor.capture());
        assertFalse(captor.getValue().getIsActive());
    }

    @Test @DisplayName("Should throw productStockNotFoundException if not not exists ProductStock by productSKUId")
    void deleteByProductSKUIdTestCase2() {
        // Given
        UUID productSKUId = UUID.randomUUID();

        when(productStockRepository.findByProductSKU_id(productSKUId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductStockNotFoundException.class, () -> {
            adminProductStockService.deleteByProductSKUId(productSKUId);
        });

        verify(productStockRepository).findByProductSKU_id(productSKUId);
        verify(productStockRepository, never()).save(any());
    }
}