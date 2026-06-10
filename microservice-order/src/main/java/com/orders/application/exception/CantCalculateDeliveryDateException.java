package com.orders.application.exception;

public class CantCalculateDeliveryDateException extends RuntimeException{
    public CantCalculateDeliveryDateException(String message) {
        super(message);
    }
}
