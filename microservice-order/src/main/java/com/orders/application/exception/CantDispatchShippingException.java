package com.orders.application.exception;

public class CantDispatchShippingException extends RuntimeException {
    public CantDispatchShippingException(String message) {
        super(message);
    }
}
