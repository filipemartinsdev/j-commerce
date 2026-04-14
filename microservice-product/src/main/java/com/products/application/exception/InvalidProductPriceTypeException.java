package com.products.application.exception;

public class InvalidProductPriceTypeException extends RuntimeException {
    public InvalidProductPriceTypeException(String message) {
        super(message);
    }
}
