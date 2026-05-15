package com.orders.application.service;

import com.orders.domain.entity.StorageAddress;
import org.springframework.stereotype.Service;

@Service
public interface GeocodingService {
    Point toCoordinates(Address address);

    Address toAddress(Point coordinates);

    record Point (
            double lat, double lon
    ){}

    record Address(
            String street,
            String neighborhood,
            String city,
            String zipCode,
            String stateCode,
            String countryCode
    ){};
}
