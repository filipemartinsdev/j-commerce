package com.orders.application.service.mapper;

import com.orders.application.dto.ShippingResponse;
import com.orders.domain.entity.Shipping;
import org.springframework.stereotype.Component;

@Component
public class ShippingMapper {
    public ShippingResponse toResponse(Shipping entity) {
        return new ShippingResponse(
                entity.getId(),
                entity.getStatus().getName(),
                entity.getSalesOrder().getId(),
                entity.getDeliveryAddress().getId(),
                entity.getExpectedDeliveryDate(),
                entity.getDriverId(),
                entity.getCreatedAt()
        );
    }
}
