package com.orders.application.service;

import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public interface DeliveryDateCalculator {
    Instant getDeliveryDate(Double lat, Double lon);
}
