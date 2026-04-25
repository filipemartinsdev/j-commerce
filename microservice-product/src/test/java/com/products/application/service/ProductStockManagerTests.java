package com.products.application.service;

import com.products.application.exception.ProductOutOfStockException;
import com.products.application.exception.ProductStockNotFoundException;
import com.products.domain.entity.ProductStock;
import com.products.infra.persistence.ProductStockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductStockManagerTests {

    @Mock
    private ProductStockRepository productStockRepository;

    @InjectMocks
    private ProductStockManager productStockManager;

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

        productStockManager.reduceProductStock(productSKUId, units);

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

        productStockManager.reduceProductStock(productSKUId, units);

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
                productStockManager.reduceProductStock(productSKUId, units)
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
                productStockManager.reduceProductStock(productSKUId, units)
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
                productStockManager.reduceProductStock(productSKUId, units)
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
                productStockManager.reduceProductStock(productSKUId, units)
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
                productStockManager.reduceProductStock(productSKUId, units)
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

        productStockManager.reduceProductStock(productSKUId, units);

        assertEquals(4, stock.getUnits());
    }
}