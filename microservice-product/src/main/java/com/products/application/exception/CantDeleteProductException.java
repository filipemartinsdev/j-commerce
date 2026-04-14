package com.products.application.exception;

public class CantDeleteProductException extends RuntimeException {
    public CantDeleteProductException(String message) {
        super(message);
    }
}
