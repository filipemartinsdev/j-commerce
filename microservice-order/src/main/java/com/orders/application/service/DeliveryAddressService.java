package com.orders.application.service;

import com.orders.application.dto.*;
import com.orders.application.exception.AddressByCoordinatesClientBadGateway;
import com.orders.application.exception.DeliveryAddressNotFoundException;
import com.orders.application.exception.InvalidDeliveryAddressCoordinatesException;
import com.orders.application.exception.InvalidDeliveryAddressException;
import com.orders.application.service.mapper.DeliveryAddressMapper;
import com.orders.domain.entity.DeliveryAddress;
import com.orders.infra.persistence.DeliveryAddressRepository;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeliveryAddressService {
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final DeliveryAddressMapper deliveryAddressMapper;
    private final AddressByCoordinatesClient addressByCoordinatesClient;

    public DeliveryAddressService(DeliveryAddressRepository deliveryAddressRepository, DeliveryAddressMapper deliveryAddressMapper, AddressByCoordinatesClient addressByCoordinatesClient) {
        this.deliveryAddressRepository = deliveryAddressRepository;
        this.deliveryAddressMapper = deliveryAddressMapper;
        this.addressByCoordinatesClient = addressByCoordinatesClient;
    }

    public PagedResponse<DeliveryAddressResponse> getAllByUserId(UUID userId, Pageable pageable) {
        Page<DeliveryAddress> page = deliveryAddressRepository.findAllActiveByUserId(userId, pageable);

        return PagedResponse.<DeliveryAddressResponse>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .isLast(page.isLast())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .content(page.getContent().stream()
                        .map(deliveryAddressMapper::toResponse)
                        .toList()
                )
                .build();
    }

    public DeliveryAddressResponse createByUserId(CreateDeliveryAddressRequest request, UUID userId) {
        validateRequestToCreateByUserId(request);

        DeliveryAddress address = deliveryAddressMapper.toEntity(request);
        address.setUserId(userId);

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

//    TODO: FIX UNIT TESTS FOR THIS NEW METHOD
    public DeliveryAddressResponse createByCoordinatesAndUserId(CreateDeliveryAddressRequest request, UUID userId) {
        if (request.latitude().isEmpty() || request.longitude().isEmpty())
            throw new InvalidDeliveryAddressCoordinatesException("Latitude and Longitude is mandatory");

        AddressByCoordinatesResponse addressResponse = requestAddress(
                request.latitude().get(),
                request.longitude().get());

        if (!isAddressFromBrazil(addressResponse))
            throw new InvalidDeliveryAddressCoordinatesException("Address is not from Brazil");

        if (addressResponse.address().zipCode() == null || addressResponse.address().road() == null)
            throw new InvalidDeliveryAddressCoordinatesException("Invalid address");

        return deliveryAddressMapper.toResponse(registerAddressByCoordinatesResponse(request, addressResponse, userId));
    }

    private AddressByCoordinatesResponse requestAddress(double lat, double lon){
        ResponseEntity<AddressByCoordinatesResponse> response = addressByCoordinatesClient.getAddressByCoordinates(
                lat, lon, "json"
        );

        if (response.getStatusCode().is4xxClientError())
            throw new InvalidDeliveryAddressCoordinatesException("Invalid address coordinates");

        if (response.getStatusCode().is2xxSuccessful())
            return response.getBody();

        else
            throw new AddressByCoordinatesClientBadGateway("Coordinates service is unavailable");
    }

    private boolean isAddressFromBrazil(AddressByCoordinatesResponse response) {
        return response.address().countryCode().equals("br");
    }

    private DeliveryAddress registerAddressByCoordinatesResponse(CreateDeliveryAddressRequest request, AddressByCoordinatesResponse addressByCoordinatesResponse, UUID userId) {
        DeliveryAddress address = deliveryAddressMapper.toEntity(addressByCoordinatesResponse);

        address.setUserId(userId);
        address.setLatitude(request.latitude().get());
        address.setLongitude(request.longitude().get());

        if (request.complement().isPresent())
            address.setComplement(request.complement().get());

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
