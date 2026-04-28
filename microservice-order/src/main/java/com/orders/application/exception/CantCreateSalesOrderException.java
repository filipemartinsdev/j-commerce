package com.orders.application.exception;

public class CantCreateSalesOrderException extends RuntimeException {
    public CantCreateSalesOrderException(String message) {
        super(message);
    }
}
