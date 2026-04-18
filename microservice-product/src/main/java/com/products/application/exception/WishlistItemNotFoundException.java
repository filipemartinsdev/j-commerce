package com.products.application.exception;

public class WishlistItemNotFoundException extends RuntimeException {
    public WishlistItemNotFoundException(String message) {
        super(message);
    }
}
