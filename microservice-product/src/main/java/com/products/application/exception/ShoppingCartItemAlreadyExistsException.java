package com.products.application.exception;

public class ShoppingCartItemAlreadyExistsException extends RuntimeException {
    public ShoppingCartItemAlreadyExistsException(String message) {
        super(message);
    }
}
