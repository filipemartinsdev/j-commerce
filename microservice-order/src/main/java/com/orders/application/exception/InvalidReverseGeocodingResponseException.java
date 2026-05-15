package com.orders.application.exception;

public class InvalidReverseGeocodingResponseException extends RuntimeException {
    public InvalidReverseGeocodingResponseException(String message) {
        super(message);
    }
}
