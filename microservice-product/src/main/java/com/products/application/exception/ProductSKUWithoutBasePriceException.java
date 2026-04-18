package com.products.application.exception;

public class ProductSKUWithoutBasePriceException extends RuntimeException {
    public ProductSKUWithoutBasePriceException(String message) {
        super(message);
    }
}
