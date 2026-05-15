package com.orders.application.exception;

public class CantCheckOutShippingException extends RuntimeException {
    public CantCheckOutShippingException(String message) {
        super(message);
    }
}
