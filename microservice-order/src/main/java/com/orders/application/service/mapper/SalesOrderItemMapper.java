package com.orders.application.service.mapper;

import com.orders.application.dto.SalesOrderItemResponse;
import com.orders.domain.entity.SalesOrderItem;
import org.springframework.stereotype.Component;

@Component
public class SalesOrderItemMapper {
    public SalesOrderItemResponse toResponse(SalesOrderItem entity) {
        return new SalesOrderItemResponse(
                entity.getSku(),
                entity.getName(),
                entity.getUnitPrice(),
                entity.getUnits()
        );
    }
}
