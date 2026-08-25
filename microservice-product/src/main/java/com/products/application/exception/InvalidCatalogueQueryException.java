package com.products.application.exception;

public class InvalidCatalogueQueryException extends RuntimeException {
    public InvalidCatalogueQueryException(String message) {
        super(message);
    }
}
