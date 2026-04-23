package com.orders.application.exception;

public class DeliveryAddressNotFoundException extends RuntimeException {
    public DeliveryAddressNotFoundException(String message) {
        super(message);
    }
}
