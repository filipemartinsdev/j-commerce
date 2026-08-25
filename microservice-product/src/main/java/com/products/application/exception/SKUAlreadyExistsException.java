package com.products.application.exception;

public class SKUAlreadyExistsException extends RuntimeException {
    public SKUAlreadyExistsException(String message) {
        super(message);
    }
}
