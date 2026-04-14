package com.products.application.exception;

public class ProductNotActiveException extends RuntimeException {
    public ProductNotActiveException(String message) {
        super(message);
    }
}
