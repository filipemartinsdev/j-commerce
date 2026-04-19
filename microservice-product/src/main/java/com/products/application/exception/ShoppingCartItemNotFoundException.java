package com.products.application.exception;

public class ShoppingCartItemNotFoundException extends RuntimeException {
    public ShoppingCartItemNotFoundException(String message) {
        super(message);
    }
}
