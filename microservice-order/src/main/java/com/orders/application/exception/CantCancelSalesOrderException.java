package com.orders.application.exception;

public class CantCancelSalesOrderException extends RuntimeException {
    public CantCancelSalesOrderException(String message) {
        super(message);
    }
}
