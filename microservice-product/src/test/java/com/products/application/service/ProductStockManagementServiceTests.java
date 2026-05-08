package com.products.application.service;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.admin.CreateStockEntryRequest;
import com.products.application.dto.admin.ProductStockResponse;
import com.products.application.dto.admin.StockMovementResponse;
import com.products.application.dto.admin.StockMovementTypeResponse;
import com.products.application.exception.ProductOutOfStockException;
import com.products.application.exception.ProductSKUNotFoundException;
import com.products.application.exception.ProductStockNotFoundException;
import com.products.application.factory.PagedResponseFactory;
import com.products.application.service.mapper.ProductStockMapper;
import com.products.application.service.mapper.StockMovementMapper;
import com.products.domain.entity.ProductSKU;
import com.products.domain.entity.ProductStock;
import com.products.domain.entity.StockMovement;
import com.products.domain.entity.StockMovementType;
import com.products.infra.persistence.ProductSKURepository;
import com.products.infra.persistence.ProductStockRepository;
import com.products.infra.persistence.StockMovementRepository;
import com.products.infra.persistence.StockMovementTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductStockManagementServiceTests {
    @Mock private ProductStockRepository productStockRepository;
    @Mock private ProductStockMapper productStockMapper;
    @Mock private StockMovementTypeRepository stockMovementTypeRepository;
    @Mock private StockMovementRepository stockMovementRepository;
    @Mock private StockMovementMapper stockMovementMapper;
    @Mock private ProductSKURepository productSKURepository;
    @Mock private PagedResponseFactory<ProductStockResponse> pagedResponseFactoryProductStock;
    @Mock private PagedResponseFactory<StockMovementResponse> pagedResponseFactoryStockMovement;

    private ProductStockManagementService productStockManagementService;

    @BeforeEach
    public void setUp() {
        this.productStockManagementService = new ProductStockManagementService(
                productStockRepository,
                productStockMapper,
                stockMovementTypeRepository,
                stockMovementRepository,
                stockMovementMapper,
                productSKURepository,
                pagedResponseFactoryProductStock,
                pagedResponseFactoryStockMovement
        );
    }

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

        PagedResponse<ProductStockResponse> expectedResponse = PagedResponse.<ProductStockResponse>builder()
                .page(0)
                .size(10)
                .isLast(true)
                .totalElements(2L)
                .totalPages(1)
                .content(List.of(response1, response2))
                .build();

        when(productStockRepository.findAllActive(pageable)).thenReturn(page);
        when(pagedResponseFactoryProductStock.fromPage(any(), any())).thenReturn(expectedResponse);

        // When
        PagedResponse<ProductStockResponse> result = productStockManagementService.getAll(pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(productStockRepository)
                .findAllActive(pageable);
        verify(pagedResponseFactoryProductStock)
                .fromPage(any(), any());
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any active ProductStock")
    void getAllTestCase2() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductStock> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        PagedResponse<ProductStockResponse> expectedResponse = PagedResponse.<ProductStockResponse>builder()
                .content(new ArrayList<>())
                .size(10)
                .page(0)
                .isLast(true)
                .totalPages(0)
                .totalElements(0L)
                .build();

        when(productStockRepository.findAllActive(pageable))
                .thenReturn(emptyPage);
        when(pagedResponseFactoryProductStock.fromPage(any(), any()))
                .thenReturn(expectedResponse);

        // When
        PagedResponse<ProductStockResponse> result = productStockManagementService.getAll(pageable);

        // Then
        verify(productStockRepository, Mockito.times(1))
                .findAllActive(pageable);
        verify(pagedResponseFactoryProductStock, Mockito.times(1))
                .fromPage(any(), any());
        assertEquals(expectedResponse, result);
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

        PagedResponse<ProductStockResponse> expectedResponse = PagedResponse.<ProductStockResponse>builder()
                .page(0)
                .size(10)
                .isLast(true)
                .totalElements(1L)
                .totalPages(1)
                .content(List.of(response1))
                .build();

        when(productStockRepository.findAllActiveByProductId(productId, pageable)).thenReturn(page);
        when(pagedResponseFactoryProductStock.fromPage(any(), any())).thenReturn(expectedResponse);

        // When
        PagedResponse<ProductStockResponse> result = productStockManagementService.getAllByProductId(productId, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(productStockRepository)
                .findAllActiveByProductId(productId, pageable);
        verify(pagedResponseFactoryProductStock)
                .fromPage(any(), any());
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any active ProductStock by productSKUId")
    void getAllByProductIdTestCase2() {
        // Given
        UUID productId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductStock> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        PagedResponse<ProductStockResponse> expectedResponse = PagedResponse.<ProductStockResponse>builder()
                .content(new ArrayList<>())
                .size(10)
                .page(0)
                .isLast(true)
                .totalPages(0)
                .totalElements(0L)
                .build();

        when(productStockRepository.findAllActiveByProductId(productId, pageable)).thenReturn(emptyPage);
        when(pagedResponseFactoryProductStock.fromPage(any(), any())).thenReturn(expectedResponse);

        // When
        PagedResponse<ProductStockResponse> result = productStockManagementService.getAllByProductId(productId, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(productStockRepository).findAllActiveByProductId(productId, pageable);
        verify(pagedResponseFactoryProductStock).fromPage(any(), any());
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
        ProductStockResponse result = productStockManagementService.getById(stockId);

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
            productStockManagementService.getById(stockId);
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
        productStockManagementService.createStockEntry(request, userId);

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
            productStockManagementService.createStockEntry(request, userId);
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
            productStockManagementService.createStockEntry(request, userId);
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
        productStockManagementService.createStockToSKU(productSKU, userId);

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

        PagedResponse<StockMovementResponse> expectedResponse = PagedResponse.<StockMovementResponse>builder()
                .page(0)
                .size(10)
                .isLast(true)
                .totalElements(2L)
                .totalPages(1)
                .content(List.of(response1, response2))
                .build();

        when(stockMovementRepository.findAll(pageable)).thenReturn(page);
        when(pagedResponseFactoryStockMovement.fromPage(any(), any())).thenReturn(expectedResponse);

        // When
        PagedResponse<StockMovementResponse> result = productStockManagementService.getAllMovements(pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(stockMovementRepository)
                .findAll(pageable);
        verify(pagedResponseFactoryStockMovement)
                .fromPage(any(), any());
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any StockMovement")
    void getAllMovementsTestCase2() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<StockMovement> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        PagedResponse<StockMovementResponse> expectedResponse = PagedResponse.<StockMovementResponse>builder()
                .content(new ArrayList<>())
                .size(10)
                .page(0)
                .isLast(true)
                .totalPages(0)
                .totalElements(0L)
                .build();

        when(stockMovementRepository.findAll(pageable)).thenReturn(emptyPage);
        when(pagedResponseFactoryStockMovement.fromPage(any(), any())).thenReturn(expectedResponse);

        // When
        PagedResponse<StockMovementResponse> result = productStockManagementService.getAllMovements(pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(stockMovementRepository)
                .findAll(pageable);
        verify(pagedResponseFactoryStockMovement)
                .fromPage(any(), any());
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

        PagedResponse<StockMovementResponse> expectedResponse = PagedResponse.<StockMovementResponse>builder()
                .page(0)
                .size(10)
                .isLast(true)
                .totalElements(1L)
                .totalPages(1)
                .content(List.of(response1))
                .build();

        when(stockMovementRepository.findAllByProductSKU_id(productSKUId, pageable))
                .thenReturn(page);
        when(pagedResponseFactoryStockMovement.fromPage(any(), any()))
                .thenReturn(expectedResponse);

        // When
        PagedResponse<StockMovementResponse> result = productStockManagementService.getAllMovementsByProductSKUId(productSKUId, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(stockMovementRepository)
                .findAllByProductSKU_id(productSKUId, pageable);
        verify(pagedResponseFactoryStockMovement)
                .fromPage(any(), any());
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any StockMovement by productSKUId")
    void getAllMovementsByProductSKUIdTestCase2() {
        // Given
        UUID productSKUId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<StockMovement> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        PagedResponse<StockMovementResponse> expectedResponse = PagedResponse.<StockMovementResponse>builder()
                .content(new ArrayList<>())
                .size(10)
                .page(0)
                .isLast(true)
                .totalPages(0)
                .totalElements(0L)
                .build();

        when(stockMovementRepository.findAllByProductSKU_id(productSKUId, pageable))
                .thenReturn(emptyPage);
        when(pagedResponseFactoryStockMovement.fromPage(any(), any()))
                .thenReturn(expectedResponse);

        // When
        PagedResponse<StockMovementResponse> result = productStockManagementService.getAllMovementsByProductSKUId(productSKUId, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(stockMovementRepository)
                .findAllByProductSKU_id(productSKUId, pageable);
        verify(pagedResponseFactoryStockMovement)
                .fromPage(any(), any());
    }

    @Test @DisplayName("Should mark ProductStock as inactive by productSKUId successfully")
    void deleteByProductSKUIdTestCase1() {
        // Given
        UUID productSKUId = UUID.randomUUID();

        ProductStock stock = new ProductStock();
        stock.setId(UUID.randomUUID());
        stock.setUnits(50);
        stock.setIsActive(true);

        when(productStockRepository.findByProductSKU_id(productSKUId))
                .thenReturn(Optional.of(stock));

        // When
        productStockManagementService.deleteByProductSKUId(productSKUId);

        // Then
        verify(productStockRepository).save(stock);
        assertFalse(stock.getIsActive());
    }

    @Test @DisplayName("Should throw productStockNotFoundException if not not exists ProductStock by productSKUId")
    void deleteByProductSKUIdTestCase2() {
        // Given
        UUID productSKUId = UUID.randomUUID();

        when(productStockRepository.findByProductSKU_id(productSKUId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductStockNotFoundException.class, () -> {
            productStockManagementService.deleteByProductSKUId(productSKUId);
        });

        verify(productStockRepository).findByProductSKU_id(productSKUId);
        verify(productStockRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reduce stock successfully")
    void reduceProductStockTestCase1() {
        UUID productSKUId = UUID.randomUUID();
        int units = 5;

        ProductStock stock = new ProductStock();
        stock.setUnits(10);
        stock.setIsActive(true);

        when(productStockRepository.findByProductSKU_id(productSKUId))
                .thenReturn(Optional.of(stock));
        when(productStockRepository.save(any(ProductStock.class)))
                .thenReturn(stock);

        productStockManagementService.reduceProductStock(productSKUId, units);

        assertEquals(5, stock.getUnits());
        verify(productStockRepository).save(stock);
    }

    @Test
    @DisplayName("Should reduce stock to zero")
    void reduceProductStockTestCase2() {
        UUID productSKUId = UUID.randomUUID();
        int units = 10;

        ProductStock stock = new ProductStock();
        stock.setUnits(10);
        stock.setIsActive(true);

        when(productStockRepository.findByProductSKU_id(productSKUId))
                .thenReturn(Optional.of(stock));
        when(productStockRepository.save(any(ProductStock.class)))
                .thenReturn(stock);

        productStockManagementService.reduceProductStock(productSKUId, units);

        assertEquals(0, stock.getUnits());
    }

    @Test
    @DisplayName("Should throw ProductStockNotFoundException when stock not found")
    void reduceProductStockTestCase3() {
        UUID productSKUId = UUID.randomUUID();
        int units = 5;

        when(productStockRepository.findByProductSKU_id(productSKUId))
                .thenReturn(Optional.empty());

        assertThrows(ProductStockNotFoundException.class, () ->
                productStockManagementService.reduceProductStock(productSKUId, units)
        );
    }

    @Test
    @DisplayName("Should throw ProductStockNotFoundException when stock is inactive")
    void reduceProductStockTestCase4() {
        UUID productSKUId = UUID.randomUUID();
        int units = 5;

        ProductStock stock = new ProductStock();
        stock.setUnits(10);
        stock.setIsActive(false);

        when(productStockRepository.findByProductSKU_id(productSKUId))
                .thenReturn(Optional.of(stock));

        assertThrows(ProductStockNotFoundException.class, () ->
                productStockManagementService.reduceProductStock(productSKUId, units)
        );
    }

    @Test
    @DisplayName("Should throw ProductOutOfStockException when insufficient stock")
    void reduceProductStockTestCase5() {
        UUID productSKUId = UUID.randomUUID();
        int units = 15;

        ProductStock stock = new ProductStock();
        stock.setUnits(10);
        stock.setIsActive(true);

        when(productStockRepository.findByProductSKU_id(productSKUId))
                .thenReturn(Optional.of(stock));

        assertThrows(ProductOutOfStockException.class, () ->
                productStockManagementService.reduceProductStock(productSKUId, units)
        );
    }

    @Test
    @DisplayName("Should throw ProductOutOfStockException when stock is zero")
    void reduceProductStockTestCase6() {
        UUID productSKUId = UUID.randomUUID();
        int units = 1;

        ProductStock stock = new ProductStock();
        stock.setUnits(0);
        stock.setIsActive(true);

        when(productStockRepository.findByProductSKU_id(productSKUId))
                .thenReturn(Optional.of(stock));

        assertThrows(ProductOutOfStockException.class, () ->
                productStockManagementService.reduceProductStock(productSKUId, units)
        );
    }

    @Test
    @DisplayName("Should throw ProductOutOfStockException when units exceed stock by one")
    void reduceProductStockTestCase7() {
        UUID productSKUId = UUID.randomUUID();
        int units = 11;

        ProductStock stock = new ProductStock();
        stock.setUnits(10);
        stock.setIsActive(true);

        when(productStockRepository.findByProductSKU_id(productSKUId))
                .thenReturn(Optional.of(stock));

        assertThrows(ProductOutOfStockException.class, () ->
                productStockManagementService.reduceProductStock(productSKUId, units)
        );
    }

    @Test
    @DisplayName("Should reduce stock by one unit")
    void reduceProductStockTestCase8() {
        UUID productSKUId = UUID.randomUUID();
        int units = 1;

        ProductStock stock = new ProductStock();
        stock.setUnits(5);
        stock.setIsActive(true);

        when(productStockRepository.findByProductSKU_id(productSKUId))
                .thenReturn(Optional.of(stock));
        when(productStockRepository.save(any(ProductStock.class)))
                .thenReturn(stock);

        productStockManagementService.reduceProductStock(productSKUId, units);

        assertEquals(4, stock.getUnits());
    }
}