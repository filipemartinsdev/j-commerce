package com.products.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@org.junit.jupiter.api.extension.ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ProductDiscountCalculatorTests {

    @Test
    @DisplayName("Should return correct discount percent for valid prices")
    void getDiscountPercentTestCase1() {
        ProductDiscountCalculator calculator = new ProductDiscountCalculator();

        BigDecimal original = new BigDecimal("100.00");
        BigDecimal offer = new BigDecimal("75.00");

        int result = calculator.getDiscountPercent(original, offer);

        assertEquals(25, result);
    }

    @Test
    @DisplayName("Should return 0 when original value is null")
    void getDiscountPercentTestCase2() {
        ProductDiscountCalculator calculator = new ProductDiscountCalculator();

        int result = calculator.getDiscountPercent(null, new BigDecimal("75.00"));

        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should return 0 when offer value is null")
    void getDiscountPercentTestCase3() {
        ProductDiscountCalculator calculator = new ProductDiscountCalculator();

        int result = calculator.getDiscountPercent(new BigDecimal("100.00"), null);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should return 0 when original value is zero")
    void getDiscountPercentTestCase4() {
        ProductDiscountCalculator calculator = new ProductDiscountCalculator();

        int result = calculator.getDiscountPercent(BigDecimal.ZERO, new BigDecimal("75.00"));

        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should return 0 when original and offer values are equal")
    void getDiscountPercentTestCase5() {
        ProductDiscountCalculator calculator = new ProductDiscountCalculator();

        BigDecimal value = new BigDecimal("100.00");

        int result = calculator.getDiscountPercent(value, value);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should return 0 when original is less than offer (price increase)")
    void getDiscountPercentTestCase6() {
        ProductDiscountCalculator calculator = new ProductDiscountCalculator();

        BigDecimal original = new BigDecimal("75.00");
        BigDecimal offer = new BigDecimal("100.00");

        int result = calculator.getDiscountPercent(original, offer);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should return 100 when offer is zero")
    void getDiscountPercentTestCase7() {
        ProductDiscountCalculator calculator = new ProductDiscountCalculator();

        BigDecimal original = new BigDecimal("100.00");

        int result = calculator.getDiscountPercent(original, BigDecimal.ZERO);

        assertEquals(100, result);
    }

    @Test
    @DisplayName("Should return correct discount for 50% off")
    void getDiscountPercentTestCase8() {
        ProductDiscountCalculator calculator = new ProductDiscountCalculator();

        BigDecimal original = new BigDecimal("200.00");
        BigDecimal offer = new BigDecimal("100.00");

        int result = calculator.getDiscountPercent(original, offer);

        assertEquals(50, result);
    }

    @Test
    @DisplayName("Should return correct discount for small percentages")
    void getDiscountPercentTestCase9() {
        ProductDiscountCalculator calculator = new ProductDiscountCalculator();

        BigDecimal original = new BigDecimal("99.99");
        BigDecimal offer = new BigDecimal("79.99");

        int result = calculator.getDiscountPercent(original, offer);

        assertTrue(result > 0 && result < 100);
    }

    @Test
    @DisplayName("Should handle very small price differences")
    void getDiscountPercentTestCase10() {
        ProductDiscountCalculator calculator = new ProductDiscountCalculator();

        BigDecimal original = new BigDecimal("100.00");
        BigDecimal offer = new BigDecimal("99.99");

        int result = calculator.getDiscountPercent(original, offer);

        assertTrue(result >= 0 && result <= 1);
    }
}