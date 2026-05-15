package com.orders.application.service;

import com.orders.domain.entity.SalesOrder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public interface DeliveryDateCalculator {
    Instant getDeliveryDate(Double lat, Double lon);
}
