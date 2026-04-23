package com.products.application.exception;

public class InvalidDeliveryAddressException extends RuntimeException {
    public InvalidDeliveryAddressException(String message) {
        super(message);
    }
}
