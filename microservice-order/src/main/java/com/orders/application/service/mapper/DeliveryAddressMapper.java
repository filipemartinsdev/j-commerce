package com.orders.application.service.mapper;

import com.orders.application.dto.AddressByCoordinatesResponse;
import com.orders.application.dto.CreateDeliveryAddressRequest;
import com.orders.application.dto.DeliveryAddressResponse;
import com.orders.application.service.GeocodingService;
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
        address.setZipCode(request.zipCode().get());
        address.setStreet(request.street().get());
        address.setNumber(request.haveNumber() ? request.number().get() : "S/N");

        if (request.complement().isPresent())
            address.setComplement(request.complement().get());

        address.setNeighborhood(request.neighborhood().get());
        address.setCity(request.city().get());
        address.setState(request.state().get());

        return address;
    }

    public DeliveryAddress toEntity(GeocodingService.Address geocodedAddress){
        var address = new DeliveryAddress();

        address.setStreet(geocodedAddress.street());
        address.setNeighborhood(geocodedAddress.neighborhood());
        address.setCity(geocodedAddress.city());
        address.setZipCode(geocodedAddress.zipCode());
        address.setState(geocodedAddress.stateCode());

        return address;
    }
}
