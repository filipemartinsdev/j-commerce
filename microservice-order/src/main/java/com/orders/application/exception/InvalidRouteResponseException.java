package com.orders.application.exception;

public class InvalidRouteResponseException extends RuntimeException {
    public InvalidRouteResponseException() {
        super("Invalid route response from route calculator client");
    }
}
