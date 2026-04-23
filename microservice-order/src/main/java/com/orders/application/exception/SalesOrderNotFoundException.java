package com.orders.application.exception;

public class SalesOrderNotFoundException extends RuntimeException {
    public SalesOrderNotFoundException(String message) {
        super(message);
    }
}
