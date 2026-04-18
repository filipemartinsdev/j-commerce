package com.products.application.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ProductDiscountCalculator {
    public int getDiscountPercent(BigDecimal originalValue, BigDecimal offerValue){
        if (originalValue == null || offerValue == null || originalValue.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }

        if (originalValue.compareTo(offerValue) == 0) {
            return 0;
        }

        if (originalValue.compareTo(offerValue) < 0) {
            return 0;
        }

        BigDecimal ratio = offerValue.divide(originalValue, 4, RoundingMode.HALF_UP);
        BigDecimal percentage = ratio.multiply(BigDecimal.valueOf(100));

        return 100 - percentage.intValue();
    }
}
