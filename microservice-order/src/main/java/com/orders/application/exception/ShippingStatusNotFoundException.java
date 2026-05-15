package com.orders.application.exception;

public class ShippingStatusNotFoundException extends RuntimeException {
    public ShippingStatusNotFoundException(String message) {
        super(message);
    }
}
