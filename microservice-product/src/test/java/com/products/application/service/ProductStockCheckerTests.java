package com.products.application.service;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductStockCheckerTests {

    @Mock
    private ProductStockRepository productStockRepository;

    @InjectMocks
    private ProductStockChecker productStockChecker;

    @Test
    @DisplayName("Should return true when stock is sufficient")
    void isTheProductWithStockEnoughTestCase1() {
        UUID productSKUId = UUID.randomUUID();
        Integer units = 5;

        ProductStock stock = new ProductStock();
        stock.setUnits(10);

        when(productStockRepository.findByProductSKU_id(productSKUId))
                .thenReturn(Optional.of(stock));

        boolean result = productStockChecker.isTheProductWithStockEnough(productSKUId, units);

        assertTrue(result);
        verify(productStockRepository).findByProductSKU_id(productSKUId);
    }

    @Test
    @DisplayName("Should return false when stock is insufficient")
    void isTheProductWithStockEnoughTestCase2() {
        UUID productSKUId = UUID.randomUUID();
        Integer units = 10;

        ProductStock stock = new ProductStock();
        stock.setUnits(5);

        when(productStockRepository.findByProductSKU_id(productSKUId))
                .thenReturn(Optional.of(stock));

        boolean result = productStockChecker.isTheProductWithStockEnough(productSKUId, units);

        assertFalse(result);
        verify(productStockRepository).findByProductSKU_id(productSKUId);
    }

    @Test
    @DisplayName("Should return true when stock equals requested units")
    void isTheProductWithStockEnoughTestCase3() {
        UUID productSKUId = UUID.randomUUID();
        Integer units = 5;

        ProductStock stock = new ProductStock();
        stock.setUnits(5);

        when(productStockRepository.findByProductSKU_id(productSKUId))
                .thenReturn(Optional.of(stock));

        boolean result = productStockChecker.isTheProductWithStockEnough(productSKUId, units);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should throw ProductStockNotFoundException when stock not found")
    void isTheProductWithStockEnoughTestCase4() {
        UUID productSKUId = UUID.randomUUID();
        Integer units = 5;

        when(productStockRepository.findByProductSKU_id(productSKUId))
                .thenReturn(Optional.empty());

        assertThrows(ProductStockNotFoundException.class, () ->
                productStockChecker.isTheProductWithStockEnough(productSKUId, units)
        );
    }

    @Test
    @DisplayName("Should return false when stock is zero and units requested")
    void isTheProductWithStockEnoughTestCase5() {
        UUID productSKUId = UUID.randomUUID();
        Integer units = 5;

        ProductStock stock = new ProductStock();
        stock.setUnits(0);

        when(productStockRepository.findByProductSKU_id(productSKUId))
                .thenReturn(Optional.of(stock));

        boolean result = productStockChecker.isTheProductWithStockEnough(productSKUId, units);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return true when requesting zero units")
    void isTheProductWithStockEnoughTestCase6() {
        UUID productSKUId = UUID.randomUUID();
        Integer units = 0;

        ProductStock stock = new ProductStock();
        stock.setUnits(0);

        when(productStockRepository.findByProductSKU_id(productSKUId))
                .thenReturn(Optional.of(stock));

        boolean result = productStockChecker.isTheProductWithStockEnough(productSKUId, units);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when units requested exceeds stock by one")
    void isTheProductWithStockEnoughTestCase7() {
        UUID productSKUId = UUID.randomUUID();
        Integer units = 6;

        ProductStock stock = new ProductStock();
        stock.setUnits(5);

        when(productStockRepository.findByProductSKU_id(productSKUId))
                .thenReturn(Optional.of(stock));

        boolean result = productStockChecker.isTheProductWithStockEnough(productSKUId, units);

        assertFalse(result);
    }
}