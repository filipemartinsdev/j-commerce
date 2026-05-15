package com.orders.application.exception;

public class CantCheckInShippingException extends RuntimeException {
    public CantCheckInShippingException(String message) {
        super(message);
    }
}
