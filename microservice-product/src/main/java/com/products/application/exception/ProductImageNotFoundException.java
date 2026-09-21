package com.products.application.exception;

public class ProductImageNotFoundException extends RuntimeException {
    public ProductImageNotFoundException(String message) {
        super(message);
    }
}
