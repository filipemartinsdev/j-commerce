package com.orders.application.service;

import com.orders.application.dto.CreateDeliveryAddressRequest;
import com.orders.application.dto.DeliveryAddressResponse;
import com.orders.application.dto.PagedResponse;
import com.orders.application.dto.UpdateDeliveryAddressRequest;
import com.orders.application.exception.DeliveryAddressNotFoundException;
import com.orders.application.service.mapper.DeliveryAddressMapper;
import com.orders.domain.entity.DeliveryAddress;
import com.orders.infra.persistence.DeliveryAddressRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeliveryAddressServiceTests {

    @Mock
    private DeliveryAddressRepository deliveryAddressRepository;

    @Mock
    private DeliveryAddressMapper deliveryAddressMapper;

    @InjectMocks
    private DeliveryAddressService deliveryAddressService;

    @Test
    @DisplayName("Should return paginated addresses")
    void getAllByUserIdTestCase1() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        DeliveryAddress address = new DeliveryAddress();
        address.setId(UUID.randomUUID());
        address.setUserId(userId);
        address.setStreet("Test Street");
        address.setZipCode("12345678");

        Page<DeliveryAddress> page = new PageImpl<>(List.of(address), pageable, 1);

        DeliveryAddressResponse response = new DeliveryAddressResponse(
                address.getId(),
                address.getZipCode(),
                address.getStreet(),
                address.getNumber(),
                address.getComplement(),
                address.getNeighborhood(),
                address.getCity(),
                address.getState(),
                address.getLatitude(),
                address.getLongitude(),
                Instant.now()
        );

        when(deliveryAddressRepository.findAllActiveByUserId(userId, pageable))
                .thenReturn(page);
        when(deliveryAddressMapper.toResponse(address))
                .thenReturn(response);

        PagedResponse<DeliveryAddressResponse> result = deliveryAddressService.getAllByUserId(userId, pageable);

        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals("Test Street", result.content().get(0).street());
        verify(deliveryAddressRepository).findAllActiveByUserId(userId, pageable);
    }

    @Test
    @DisplayName("Should return empty page when no addresses")
    void getAllByUserIdTestCase2() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Page<DeliveryAddress> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(deliveryAddressRepository.findAllActiveByUserId(userId, pageable))
                .thenReturn(emptyPage);

        PagedResponse<DeliveryAddressResponse> result = deliveryAddressService.getAllByUserId(userId, pageable);

        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
        verify(deliveryAddressRepository).findAllActiveByUserId(userId, pageable);
    }

    @Test
    @DisplayName("Should create new address")
    void createByUserIdTestCase1() {
        UUID userId = UUID.randomUUID();

        CreateDeliveryAddressRequest request = new CreateDeliveryAddressRequest(
                "12345678",
                "Test Street",
                "123",
                Optional.of("Apt 1"),
                "Test Neighborhood",
                "Test City",
                "TS",
                Optional.of(-23.0),
                Optional.of(-46.0)
        );

        DeliveryAddress entity = new DeliveryAddress();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);

        DeliveryAddressResponse response = new DeliveryAddressResponse(
                entity.getId(),
                "12345678",
                "Test Street",
                "123",
                "Apt 1",
                "Test Neighborhood",
                "Test City",
                "TS",
                -23.0,
                -46.0,
                Instant.now()
        );

        when(deliveryAddressMapper.toEntity(request))
                .thenReturn(entity);
        when(deliveryAddressRepository.save(any(DeliveryAddress.class)))
                .thenReturn(entity);
        when(deliveryAddressMapper.toResponse(entity))
                .thenReturn(response);

        DeliveryAddressResponse result = deliveryAddressService.createByUserId(request, userId);

        assertNotNull(result);
        assertEquals("12345678", result.zipCode());
        verify(deliveryAddressRepository).save(any(DeliveryAddress.class));
    }

    @Test
    @DisplayName("Should return address by id")
    void getByIdTestCase1() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        DeliveryAddress address = new DeliveryAddress();
        address.setId(id);
        address.setUserId(userId);
        address.setStreet("Test Street");

        DeliveryAddressResponse response = new DeliveryAddressResponse(
                id,
                "12345678",
                "Test Street",
                "123",
                null,
                "Test Neighborhood",
                "Test City",
                "TS",
                null,
                null,
                Instant.now()
        );

        when(deliveryAddressRepository.findActiveByIdAndUserId(id, userId))
                .thenReturn(Optional.of(address));
        when(deliveryAddressMapper.toResponse(address))
                .thenReturn(response);

        DeliveryAddressResponse result = deliveryAddressService.getById(id, userId);

        assertNotNull(result);
        assertEquals("Test Street", result.street());
        verify(deliveryAddressRepository).findActiveByIdAndUserId(id, userId);
    }

    @Test
    @DisplayName("Should throw DeliveryAddressNotFoundException when not found")
    void getByIdTestCase2() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(deliveryAddressRepository.findActiveByIdAndUserId(id, userId))
                .thenReturn(Optional.empty());

        assertThrows(DeliveryAddressNotFoundException.class, () -> {
            deliveryAddressService.getById(id, userId);
        });

        verify(deliveryAddressRepository).findActiveByIdAndUserId(id, userId);
    }

    @Test
    @DisplayName("Should mark address as inactive by id")
    void deleteByIdTestCase1() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        DeliveryAddress address = new DeliveryAddress();
        address.setId(id);
        address.setUserId(userId);
        address.setIsActive(true);

        when(deliveryAddressRepository.findActiveByIdAndUserId(id, userId))
                .thenReturn(Optional.of(address));
        when(deliveryAddressRepository.save(any(DeliveryAddress.class)))
                .thenReturn(address);

        deliveryAddressService.deleteById(id, userId);

        assertFalse(address.getIsActive());
        verify(deliveryAddressRepository).save(address);
        verify(deliveryAddressRepository, Mockito.never()).delete(any());
        verify(deliveryAddressRepository, Mockito.never()).deleteById(any());
    }

    @Test
    @DisplayName("Should throw DeliveryAddressNotFoundException when address not exists or is inactive")
    void deleteByIdTestCase2() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(deliveryAddressRepository.findActiveByIdAndUserId(id, userId))
                .thenReturn(Optional.empty());

        assertThrows(DeliveryAddressNotFoundException.class, () -> {
            deliveryAddressService.deleteById(id, userId);
        });

        verify(deliveryAddressRepository).findActiveByIdAndUserId(id, userId);
    }

    @Test
    @DisplayName("Should update address by id")
    void updateByIdTestCase1() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UpdateDeliveryAddressRequest request = new UpdateDeliveryAddressRequest(
                Optional.of("87654321"),
                Optional.of("Updated Street"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        DeliveryAddress address = new DeliveryAddress();
        address.setId(id);
        address.setUserId(userId);
        address.setZipCode("12345678");
        address.setStreet("Test Street");

        DeliveryAddressResponse response = new DeliveryAddressResponse(
                id,
                "87654321",
                "Updated Street",
                "123",
                null,
                "Test Neighborhood",
                "Test City",
                "TS",
                null,
                null,
                Instant.now()
        );

        when(deliveryAddressRepository.findActiveByIdAndUserId(id, userId))
                .thenReturn(Optional.of(address));
        when(deliveryAddressRepository.save(any(DeliveryAddress.class)))
                .thenReturn(address);
        when(deliveryAddressMapper.toResponse(address))
                .thenReturn(response);

        DeliveryAddressResponse result = deliveryAddressService.updateById(id, userId, request);

        assertNotNull(result);
        verify(deliveryAddressRepository).save(address);
    }

    @Test
    @DisplayName("Should throw DeliveryAddressNotFoundException when updating not found")
    void updateByIdTestCase2() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UpdateDeliveryAddressRequest request = new UpdateDeliveryAddressRequest(
                Optional.of("87654321"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        when(deliveryAddressRepository.findActiveByIdAndUserId(id, userId))
                .thenReturn(Optional.empty());

        assertThrows(DeliveryAddressNotFoundException.class, () -> {
            deliveryAddressService.updateById(id, userId, request);
        });

        verify(deliveryAddressRepository).findActiveByIdAndUserId(id, userId);
    }
}