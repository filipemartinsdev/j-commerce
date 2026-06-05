package com.orders.application.service;

import com.orders.application.dto.CreateDeliveryAddressRequest;
import com.orders.application.dto.DeliveryAddressResponse;
import com.orders.application.dto.UpdateDeliveryAddressRequest;
import com.orders.application.exception.DeliveryAddressNotFoundException;
import com.orders.application.exception.InvalidDeliveryAddressCoordinatesException;
import com.orders.application.exception.InvalidDeliveryAddressException;
import com.orders.application.service.mapper.DeliveryAddressMapper;
import com.orders.domain.entity.DeliveryAddress;
import com.orders.infra.persistence.DeliveryAddressRepository;
import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.spring.PagedResponseFactory;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class DeliveryAddressService {
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final DeliveryAddressMapper deliveryAddressMapper;
    private final GeocodingService geocodingService;

    public DeliveryAddressService(DeliveryAddressRepository deliveryAddressRepository, DeliveryAddressMapper deliveryAddressMapper, GeocodingService geocodingService) {
        this.deliveryAddressRepository = deliveryAddressRepository;
        this.deliveryAddressMapper = deliveryAddressMapper;
        this.geocodingService = geocodingService;
    }

    public PagedResponse<DeliveryAddressResponse> getAllByUserId(UUID userId, Pageable pageable) {
        Page<DeliveryAddress> page = deliveryAddressRepository.findAllActiveByUserId(userId, pageable);

        return PagedResponseFactory.fromPage(page, deliveryAddressMapper::toResponse);
    }

    public DeliveryAddressResponse createByUserId(CreateDeliveryAddressRequest request, UUID userId) {
        validateRequestToCreateByUserId(request);

        DeliveryAddress address = deliveryAddressMapper.toEntity(request);
        address.setUserId(userId);

        GeocodingService.Point point = geocodingService.toCoordinates(
                new GeocodingService.Address(
                        request.street().get(),
                        request.neighborhood().get(),
                        request.city().get(),
                        request.zipCode().get(),
                        request.state().get(),
                        "BR"
                )
        );

        address.setLatitude(point.lat());
        address.setLongitude(point.lon());

        if (request.haveNumber())
            address.setNumber(request.number().get());

        return deliveryAddressMapper.toResponse(deliveryAddressRepository.save(address));
    }

    private void validateRequestToCreateByUserId(CreateDeliveryAddressRequest request) {
        if (request.haveNumber() && request.number().isEmpty())
            throw new InvalidDeliveryAddressException("Address number is mandatory");

        if (
                request.zipCode().isEmpty() ||
                request.street().isEmpty() ||
                request.neighborhood().isEmpty() ||
                request.city().isEmpty() ||
                request.state().isEmpty()
        ) {
            throw new InvalidDeliveryAddressException("Invalid delivery address");
        }
    }

    public DeliveryAddressResponse createByCoordinatesAndUserId(CreateDeliveryAddressRequest request, UUID userId) {
        if (request.latitude().isEmpty() || request.longitude().isEmpty())
            throw new InvalidDeliveryAddressCoordinatesException("Latitude and Longitude is mandatory");

        GeocodingService.Address geocodedAddress = geocodingService.toAddress(
                new GeocodingService.Point(
                        request.latitude().get(), request.longitude().get()
                )
        );

        if (!isAddressFromBrazil(geocodedAddress))
            throw new InvalidDeliveryAddressCoordinatesException("Address is not from Brazil");

        return deliveryAddressMapper.toResponse(
                registerGeocodedAddress(
                        geocodedAddress,
                        request.latitude().get(),
                        request.longitude().get(),
                        request.complement(),
                        userId
                )
        );
    }

    private boolean isAddressFromBrazil(GeocodingService.Address response) {
        return response.countryCode().equalsIgnoreCase("br");
    }

    private DeliveryAddress registerGeocodedAddress(GeocodingService.Address geocodedAddress, double lat, double lon, Optional<String> complement, UUID userId) {
        DeliveryAddress address = deliveryAddressMapper.toEntity(geocodedAddress);

        address.setNumber("S/N");
        address.setUserId(userId);
        address.setLatitude(lat);
        address.setLongitude(lon);

        if (complement.isPresent())
            address.setComplement(complement.get());

        return deliveryAddressRepository.save(address);
    }

    public DeliveryAddressResponse getById(UUID id, UUID userId) {
        DeliveryAddress address = deliveryAddressRepository.findActiveByIdAndUserId(id, userId)
                .orElseThrow(() -> new DeliveryAddressNotFoundException("Delivery address not found with ID: "+id));

        return deliveryAddressMapper.toResponse(address);
    }

    public void deleteById(UUID id, UUID userId) {
        DeliveryAddress address = deliveryAddressRepository.findActiveByIdAndUserId(id, userId)
                .orElseThrow(() -> new DeliveryAddressNotFoundException("Delivery address not found with ID: "+id));

        address.setIsActive(false);

        deliveryAddressRepository.save(address);
    }

    public DeliveryAddressResponse updateById(UUID id, UUID userId, @Valid UpdateDeliveryAddressRequest request) {
        DeliveryAddress address = deliveryAddressRepository.findActiveByIdAndUserId(id, userId)
                .orElseThrow(() -> new DeliveryAddressNotFoundException("Delivery address not found with ID: "+id));

        if (request.zipCode().isPresent())
            address.setZipCode(request.zipCode().get());

        if (request.street().isPresent())
            address.setStreet(request.street().get());

        if (request.number().isPresent())
            address.setNumber(request.number().get());

        if (request.complement().isPresent())
            address.setComplement(request.complement().get());

        if (request.neighborhood().isPresent())
            address.setNeighborhood(request.neighborhood().get());

        if (request.city().isPresent())
            address.setCity(request.city().get());

        if (request.state().isPresent())
            address.setState(request.state().get());

        if (request.latitude().isPresent())
            address.setLatitude(request.latitude().get());

        if (request.longitude().isPresent())
            address.setLongitude(request.longitude().get());

        return deliveryAddressMapper.toResponse(
                deliveryAddressRepository.save(address)
        );
    }
}
