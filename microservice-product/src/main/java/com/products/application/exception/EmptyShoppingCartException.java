package com.products.application.exception;

public class EmptyShoppingCartException extends RuntimeException {
    public EmptyShoppingCartException(String message) {
        super(message);
    }
}
