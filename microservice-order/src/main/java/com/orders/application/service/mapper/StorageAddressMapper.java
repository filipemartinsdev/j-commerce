package com.orders.application.service.mapper;

import com.orders.application.dto.AddressByCoordinatesResponse;
import com.orders.application.dto.StorageAddressRequest;
import com.orders.application.dto.StorageAddressResponse;
import com.orders.application.service.GeocodingService;
import com.orders.domain.entity.StorageAddress;
import org.springframework.stereotype.Component;

@Component
public class StorageAddressMapper {
    public StorageAddressResponse toResponse(StorageAddress entity) {
        return new StorageAddressResponse(
                entity.getId(),
                entity.getZipCode(),
                entity.getStreet(),
                entity.getComplement(),
                entity.getNeighborhood(),
                entity.getCity(),
                entity.getState(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getCreatedAt()
        );
    }

    public StorageAddress toEntity(StorageAddressRequest request) {
        var address = new StorageAddress();
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

    public StorageAddress toEntity(GeocodingService.Address geocodingResponse) {
        var address = new StorageAddress();

        address.setStreet(geocodingResponse.street());
        address.setNeighborhood(geocodingResponse.neighborhood());
        address.setCity(geocodingResponse.city());
        address.setState(geocodingResponse.stateCode());
        address.setZipCode(geocodingResponse.zipCode());

        return address;
    }
}
