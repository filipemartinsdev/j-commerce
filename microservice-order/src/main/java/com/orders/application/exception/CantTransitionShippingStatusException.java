package com.orders.application.exception;

// TODO: handle
public class CantTransitionShippingStatusException extends RuntimeException {
    public CantTransitionShippingStatusException(String message) {
        super(message);
    }
}
