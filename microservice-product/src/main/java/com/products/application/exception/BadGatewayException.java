package com.products.application.exception;

public class BadGatewayException extends RuntimeException {
    public BadGatewayException(String message) {
        super(message);
    }

    public BadGatewayException() {
        super("External service error");
    }
}
