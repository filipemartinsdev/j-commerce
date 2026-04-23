package com.orders.application.service;

import com.orders.application.dto.*;
import com.orders.application.exception.DeliveryAddressNotFoundException;
import com.orders.application.service.mapper.DeliveryAddressMapper;
import com.orders.domain.entity.DeliveryAddress;
import com.orders.infra.persistence.DeliveryAddressRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeliveryAddressService {
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final DeliveryAddressMapper deliveryAddressMapper;

    public DeliveryAddressService(DeliveryAddressRepository deliveryAddressRepository, DeliveryAddressMapper deliveryAddressMapper) {
        this.deliveryAddressRepository = deliveryAddressRepository;
        this.deliveryAddressMapper = deliveryAddressMapper;
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
        DeliveryAddress address = deliveryAddressMapper.toEntity(request);
        address.setUserId(userId);

        return deliveryAddressMapper.toResponse(deliveryAddressRepository.save(address));
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
