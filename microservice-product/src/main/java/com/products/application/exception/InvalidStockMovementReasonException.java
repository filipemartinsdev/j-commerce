package com.products.application.exception;

public class InvalidStockMovementReasonException extends RuntimeException {
    public InvalidStockMovementReasonException(String message) {
        super(message);
    }
}
