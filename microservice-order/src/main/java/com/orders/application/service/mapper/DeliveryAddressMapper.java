package com.orders.application.service.mapper;

import com.orders.application.dto.CreateDeliveryAddressRequest;
import com.orders.application.dto.DeliveryAddressResponse;
import com.orders.domain.entity.DeliveryAddress;
import org.springframework.stereotype.Component;

@Component
public class DeliveryAddressMapper {
    public DeliveryAddressResponse toResponse(DeliveryAddress entity) {
        return new DeliveryAddressResponse(
                entity.getId(),
                entity.getZipCode(),
                entity.getStreet(),
                entity.getNumber(),
                entity.getComplement(),
                entity.getNeighborhood(),
                entity.getCity(),
                entity.getState(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getCreatedAt()
        );
    }

    public DeliveryAddress toEntity(CreateDeliveryAddressRequest request) {
        var address = new DeliveryAddress();
        address.setZipCode(request.zipCode());
        address.setStreet(request.street());
        address.setNumber(request.number());

        if (request.complement().isPresent())
            address.setComplement(request.complement().get());

        address.setNeighborhood(request.neighborhood());
        address.setCity(request.city());
        address.setState(request.state());

        if (request.latitude().isPresent())
            address.setLatitude(request.latitude().get());

        if (request.longitude().isPresent())
            address.setLongitude(request.longitude().get());

        return address;
    }
}
