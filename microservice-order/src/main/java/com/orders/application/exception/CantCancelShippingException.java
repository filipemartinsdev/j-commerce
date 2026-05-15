package com.orders.application.exception;

public class CantCancelShippingException extends RuntimeException {
    public CantCancelShippingException(String message) {
        super(message);
    }
}
