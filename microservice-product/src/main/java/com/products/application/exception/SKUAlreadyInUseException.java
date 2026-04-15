package com.products.application.exception;

public class SKUAlreadyInUseException extends RuntimeException {
    public SKUAlreadyInUseException(String message) {
        super(message);
    }
}
