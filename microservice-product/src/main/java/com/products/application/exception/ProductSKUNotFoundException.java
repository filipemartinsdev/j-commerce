package com.products.application.exception;

public class ProductSKUNotFoundException extends RuntimeException {
    public ProductSKUNotFoundException(String message) {
        super(message);
    }
}
