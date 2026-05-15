package com.orders.application.exception;

public class InvalidGeocodingResponseException extends RuntimeException {
    public InvalidGeocodingResponseException(String message) {
        super(message);
    }
}
