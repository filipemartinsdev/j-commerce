package com.products.application.exception;

public class InvalidProductCategoryException extends RuntimeException {
    public InvalidProductCategoryException(String message) {
        super(message);
    }
}
