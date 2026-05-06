package com.orders.application.exception;

public class InvalidDeliveryAddressCoordinatesException extends RuntimeException {
    public InvalidDeliveryAddressCoordinatesException(String message) {
        super(message);
    }
}
