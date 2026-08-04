package com.products.application.exception;


public class InvalidStockMovementTypeException extends RuntimeException {
    public InvalidStockMovementTypeException(String message) {
        super(message);
    }
}
