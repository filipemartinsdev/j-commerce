package com.orders.application.service.mapper;

import com.orders.application.dto.SalesOrderResponse;
import com.orders.application.dto.SalesOrderSummaryResponse;
import com.orders.domain.entity.SalesOrder;
import com.orders.domain.entity.SalesOrderItem;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class SalesOrderMapper {
    private final SalesOrderItemMapper salesOrderItemMapper;
    private final DeliveryAddressMapper deliveryAddressMapper;

    public SalesOrderMapper(@Lazy SalesOrderItemMapper salesOrderItemMapper, @Lazy DeliveryAddressMapper deliveryAddressMapper) {
        this.salesOrderItemMapper = salesOrderItemMapper;
        this.deliveryAddressMapper = deliveryAddressMapper;
    }

    public SalesOrderResponse toResponse(SalesOrder entity){
        return new SalesOrderResponse(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getStatus().getName(),
                getFinalValue(entity.getItems()),
                entity.getShipping().getStatus().getName()
        );
    }

    private BigDecimal getFinalValue(List<SalesOrderItem> items){
        BigDecimal value = new BigDecimal(0);

        for (SalesOrderItem item : items){
            value = value.add(item.getUnitPrice().multiply(new BigDecimal(item.getUnits())));
        }

        return value;
    }

    public SalesOrderSummaryResponse toSummaryResponse(SalesOrder entity){
        return new SalesOrderSummaryResponse(
                entity.getId(),
                entity.getStatus().getName(),
                entity.getShipping().getStatus().getName(),
                getFinalValue(entity.getItems()),
                entity.getItems().stream()
                        .map(salesOrderItemMapper::toResponse)
                        .toList(),
                deliveryAddressMapper.toResponse(entity.getShipping().getDeliveryAddress()),
                entity.getCreatedAt()
        );
    }
}
