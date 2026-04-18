package com.products.application.exception;

public class WishlistItemAlreadyExistsException extends RuntimeException {
    public WishlistItemAlreadyExistsException(String message) {
        super(message);
    }
}
