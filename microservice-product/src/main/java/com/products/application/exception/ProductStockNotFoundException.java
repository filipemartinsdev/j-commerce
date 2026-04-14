package com.products.application.exception;

public class ProductStockNotFoundException extends RuntimeException {
    public ProductStockNotFoundException(String message) {
        super(message);
    }
}
