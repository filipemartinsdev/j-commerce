package com.orders.application.exception;

public class InvalidStorageAddressException extends RuntimeException {
    public InvalidStorageAddressException(String message) {
        super(message);
    }
}
