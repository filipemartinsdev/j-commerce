package com.products.application.service;

import com.products.domain.entity.StockMovement;
import com.products.domain.entity.StockMovementType;
import com.products.infra.persistence.ProductSKURepository;
import com.products.infra.persistence.StockMovementRepository;
import com.products.infra.persistence.StockMovementTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMovementManagementServiceTests {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ProductSKURepository productSKURepository;

    @Mock
    private StockMovementTypeRepository stockMovementTypeRepository;

    @InjectMocks
    private StockMovementManagementService stockMovementManagementService;

    @Test
    @DisplayName("Should register sale successfully")
    void registerSaleTestCase1() {
        UUID productSKUId = UUID.randomUUID();
        int units = 5;
        UUID userId = UUID.randomUUID();

        when(productSKURepository.getReferenceById(productSKUId))
                .thenReturn(null);
        when(stockMovementTypeRepository.getReferenceById(StockMovementType.Value.SALE.getId()))
                .thenReturn(null);
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        stockMovementManagementService.registerSale(productSKUId, units, userId);

        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movementCaptor.capture());

        StockMovement savedMovement = movementCaptor.getValue();
        assertEquals(units, savedMovement.getUnits());
        assertEquals(userId, savedMovement.getCreatedBy());
    }

    @Test
    @DisplayName("Should register sale with one unit")
    void registerSaleTestCase2() {
        UUID productSKUId = UUID.randomUUID();
        int units = 1;
        UUID userId = UUID.randomUUID();

        when(productSKURepository.getReferenceById(productSKUId))
                .thenReturn(null);
        when(stockMovementTypeRepository.getReferenceById(StockMovementType.Value.SALE.getId()))
                .thenReturn(null);
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        stockMovementManagementService.registerSale(productSKUId, units, userId);

        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("Should register sale with large quantity")
    void registerSaleTestCase3() {
        UUID productSKUId = UUID.randomUUID();
        int units = 1000;
        UUID userId = UUID.randomUUID();

        when(productSKURepository.getReferenceById(productSKUId))
                .thenReturn(null);
        when(stockMovementTypeRepository.getReferenceById(StockMovementType.Value.SALE.getId()))
                .thenReturn(null);
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        stockMovementManagementService.registerSale(productSKUId, units, userId);

        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movementCaptor.capture());

        assertEquals(1000, movementCaptor.getValue().getUnits());
    }

    @Test
    @DisplayName("Should use correct stock movement type for sale")
    void registerSaleTestCase4() {
        UUID productSKUId = UUID.randomUUID();
        int units = 5;
        UUID userId = UUID.randomUUID();

        StockMovementType saleType = new StockMovementType();
        saleType.setId(StockMovementType.Value.SALE.getId());
        saleType.setName("SALE");

        when(productSKURepository.getReferenceById(productSKUId))
                .thenReturn(null);
        when(stockMovementTypeRepository.getReferenceById(StockMovementType.Value.SALE.getId()))
                .thenReturn(saleType);
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        stockMovementManagementService.registerSale(productSKUId, units, userId);

        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movementCaptor.capture());

        assertEquals(saleType, movementCaptor.getValue().getType());
    }
}