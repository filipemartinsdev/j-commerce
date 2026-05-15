package com.orders.application.exception;

public class CantUpdateShippingStatusException extends RuntimeException {
    public CantUpdateShippingStatusException(String message) {
        super(message);
    }
}
