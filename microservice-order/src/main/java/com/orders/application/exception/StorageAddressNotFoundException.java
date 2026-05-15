package com.orders.application.exception;

public class StorageAddressNotFoundException extends RuntimeException {
    public StorageAddressNotFoundException(String message) {
        super(message);
    }
}
