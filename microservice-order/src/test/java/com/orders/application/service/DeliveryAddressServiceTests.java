package com.orders.application.service;

import com.orders.application.dto.*;
import com.orders.application.exception.*;
import com.orders.application.factory.PagedResponseFactory;
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
    @Mock private DeliveryAddressRepository deliveryAddressRepository;
    @Mock private DeliveryAddressMapper deliveryAddressMapper;
    @Mock private PagedResponseFactory<DeliveryAddressResponse> pagedResponseFactory;
    @Mock private GeocodingService geocodingService;

    @InjectMocks private DeliveryAddressService deliveryAddressService;


    @Test @DisplayName("Should retrieve addresses by user ID successfully")
    void getAllByUserIdTestCase1() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        DeliveryAddress entity = new DeliveryAddress();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setStreet("Test Street");
        entity.setZipCode("12345678");
        entity.setNeighborhood("Test Neighborhood");
        entity.setCity("Test City");
        entity.setState("SP");
        entity.setNumber("123");
        entity.setIsActive(true);

        Page<DeliveryAddress> page = new PageImpl<>(List.of(entity), pageable, 1);
        DeliveryAddressResponse response = new DeliveryAddressResponse(
                entity.getId(), "12345678", "Test Street", "123", null,
                "Test Neighborhood", "Test City", "SP", null, null, Instant.now());

        PagedResponse<DeliveryAddressResponse> pagedResponse = PagedResponse.<DeliveryAddressResponse>builder()
                .page(0).size(10).totalElements(1L).totalPages(1)
                .isLast(true).content(List.of(response)).build();

        when(deliveryAddressRepository.findAllActiveByUserId(userId, pageable)).thenReturn(page);
        when(pagedResponseFactory.fromPage(eq(page), any())).thenReturn(pagedResponse);

        PagedResponse<DeliveryAddressResponse> result = deliveryAddressService.getAllByUserId(userId, pageable);

        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals(1, result.content().size());
    }

    @Test @DisplayName("Should return empty PagedResponse if not exists any address")
    void getAllByUserIdTestCase2() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Page<DeliveryAddress> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        PagedResponse<DeliveryAddressResponse> pagedResponse = PagedResponse.<DeliveryAddressResponse>builder()
                .page(0).size(10).totalElements(0L).totalPages(0)
                .isLast(true).content(List.of()).build();

        when(deliveryAddressRepository.findAllActiveByUserId(userId, pageable)).thenReturn(emptyPage);
        when(pagedResponseFactory.fromPage(eq(emptyPage), any())).thenReturn(pagedResponse);

        PagedResponse<DeliveryAddressResponse> result = deliveryAddressService.getAllByUserId(userId, pageable);

        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
    }

    @Test @DisplayName("Should create address successfully")
    void createByUserIdTestCase1() {
        UUID userId = UUID.randomUUID();
        CreateDeliveryAddressRequest request = new CreateDeliveryAddressRequest(
                true,
                Optional.of("12345678"),
                Optional.of("Test Street"),
                Optional.of("123"),
                Optional.of("Complement"),
                Optional.of("Neighborhood"),
                Optional.of("City"),
                Optional.of("SP"),
                Optional.empty(),
                Optional.empty()
        );

        DeliveryAddress entity = new DeliveryAddress();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setZipCode("12345678");
        entity.setStreet("Test Street");
        entity.setNumber("123");
        entity.setComplement("Complement");
        entity.setNeighborhood("Neighborhood");
        entity.setCity("City");
        entity.setState("SP");
        entity.setLatitude(-23.0);
        entity.setLongitude(-46.0);
        entity.setIsActive(true);

        DeliveryAddressResponse response = new DeliveryAddressResponse(
                entity.getId(), "12345678", "Test Street", "123", "Complement",
                "Neighborhood", "City", "SP", -23.0, -46.0, Instant.now());

        when(deliveryAddressMapper.toEntity(request)).thenReturn(entity);
        when(geocodingService.toCoordinates(any())).thenReturn(new GeocodingService.Point(-23.0, -46.0));
        when(deliveryAddressRepository.save(entity)).thenReturn(entity);
        when(deliveryAddressMapper.toResponse(entity)).thenReturn(response);

        DeliveryAddressResponse result = deliveryAddressService.createByUserId(request, userId);

        assertNotNull(result);
        assertEquals("12345678", result.zipCode());
        assertEquals("Test Street", result.street());
    }

    @Test @DisplayName("Should throw InvalidDeliveryAddressException if address is invalid")
    void createByUserIdTestCase2() {
        UUID userId = UUID.randomUUID();
        CreateDeliveryAddressRequest request = new CreateDeliveryAddressRequest(
                true,
                Optional.of("12345678"),
                Optional.empty(),
                Optional.of("123"),
                Optional.of("Complement"),
                Optional.of("Neighborhood"),
                Optional.of("City"),
                Optional.of("SP"),
                Optional.empty(),
                Optional.empty()
        );

        assertThrows(InvalidDeliveryAddressException.class, () -> {
            deliveryAddressService.createByUserId(request, userId);
        });
    }

    @Test @DisplayName("Should create address by coordinates successfully")
    void createByCoordinatesAndUserIdTestCase1() {
        UUID userId = UUID.randomUUID();
        CreateDeliveryAddressRequest request = new CreateDeliveryAddressRequest(
                false,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("Complement"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(-23.0),
                Optional.of(-46.0)
        );

        GeocodingService.Address geocodedAddress = new GeocodingService.Address(
                "Street", "Neighborhood", "City", "12345678", "SP", "br"
        );

        DeliveryAddress entity = new DeliveryAddress();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setStreet("Street");
        entity.setNeighborhood("Neighborhood");
        entity.setCity("City");
        entity.setZipCode("12345678");
        entity.setState("SP");
        entity.setNumber("S/N");
        entity.setComplement("Complement");
        entity.setLatitude(-23.0);
        entity.setLongitude(-46.0);
        entity.setIsActive(true);

        DeliveryAddressResponse response = new DeliveryAddressResponse(
                entity.getId(), "12345678", "Street", "S/N", "Complement",
                "Neighborhood", "City", "SP", -23.0, -46.0, Instant.now());

        when(geocodingService.toAddress(any())).thenReturn(geocodedAddress);
        when(deliveryAddressMapper.toEntity(geocodedAddress)).thenReturn(entity);
        when(deliveryAddressRepository.save(entity)).thenReturn(entity);
        when(deliveryAddressMapper.toResponse(entity)).thenReturn(response);

        DeliveryAddressResponse result = deliveryAddressService.createByCoordinatesAndUserId(request, userId);

        assertNotNull(result);
        assertEquals("Street", result.street());
    }

    @Test @DisplayName("Should throw InvalidDeliveryAddressCoordinatesException if coordinates are not present")
    void createByCoordinatesAndUserIdTestCase2() {
        UUID userId = UUID.randomUUID();
        CreateDeliveryAddressRequest request = new CreateDeliveryAddressRequest(
                false,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        assertThrows(InvalidDeliveryAddressCoordinatesException.class, () -> {
            deliveryAddressService.createByCoordinatesAndUserId(request, userId);
        });
    }

    @Test @DisplayName("Should throw InvalidDeliveryAddressCoordinatesException if coordinates are not from Brazil")
    void createByCoordinatesAndUserIdTestCase3() {
        UUID userId = UUID.randomUUID();
        CreateDeliveryAddressRequest request = new CreateDeliveryAddressRequest(
                false,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(-23.0),
                Optional.of(-46.0)
        );

        GeocodingService.Address geocodedAddress = new GeocodingService.Address(
                "Street", "Neighborhood", "City", "12345678", "SP", "us"
        );

        when(geocodingService.toAddress(any())).thenReturn(geocodedAddress);

        assertThrows(InvalidDeliveryAddressCoordinatesException.class, () -> {
            deliveryAddressService.createByCoordinatesAndUserId(request, userId);
        });
    }

    @Test @DisplayName("Should retrieve address by ID successfully")
    void getByIdTestCase1() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        DeliveryAddress entity = new DeliveryAddress();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setZipCode("12345678");
        entity.setStreet("Test Street");
        entity.setNumber("123");
        entity.setNeighborhood("Neighborhood");
        entity.setCity("City");
        entity.setState("SP");
        entity.setIsActive(true);

        DeliveryAddressResponse response = new DeliveryAddressResponse(
                id, "12345678", "Test Street", "123", null,
                "Neighborhood", "City", "SP", null, null, Instant.now());

        when(deliveryAddressRepository.findActiveByIdAndUserId(id, userId)).thenReturn(Optional.of(entity));
        when(deliveryAddressMapper.toResponse(entity)).thenReturn(response);

        DeliveryAddressResponse result = deliveryAddressService.getById(id, userId);

        assertNotNull(result);
        assertEquals(id, result.id());
        verify(deliveryAddressRepository).findActiveByIdAndUserId(id, userId);
    }

    @Test @DisplayName("Should throw DeliveryAddressNotFoundException if not found")
    void getByIdTestCase2() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(deliveryAddressRepository.findActiveByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThrows(DeliveryAddressNotFoundException.class, () -> {
            deliveryAddressService.getById(id, userId);
        });
    }

    @Test @DisplayName("Should mark address as inactive successfully")
    void deleteByIdTestCase1() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        DeliveryAddress entity = new DeliveryAddress();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setIsActive(true);

        when(deliveryAddressRepository.findActiveByIdAndUserId(id, userId)).thenReturn(Optional.of(entity));
        when(deliveryAddressRepository.save(entity)).thenReturn(entity);

        deliveryAddressService.deleteById(id, userId);

        assertFalse(entity.getIsActive());
        verify(deliveryAddressRepository).save(entity);
    }

    @Test @DisplayName("Should throw DeliveryAddressNotFoundException if not found")
    void deleteByIdTestCase2() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(deliveryAddressRepository.findActiveByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThrows(DeliveryAddressNotFoundException.class, () -> {
            deliveryAddressService.deleteById(id, userId);
        });
    }

    @Test @DisplayName("Should update address successfully")
    void updateByIdTestCase1() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UpdateDeliveryAddressRequest request = new UpdateDeliveryAddressRequest(
                Optional.of("87654321"),
                Optional.of("New Street"),
                Optional.of("456"),
                Optional.of("New Complement"),
                Optional.of("New Neighborhood"),
                Optional.of("New City"),
                Optional.of("RJ"),
                Optional.empty(),
                Optional.empty()
        );

        DeliveryAddress entity = new DeliveryAddress();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setZipCode("12345678");
        entity.setStreet("Old Street");
        entity.setNumber("123");
        entity.setComplement("Old Complement");
        entity.setNeighborhood("Old Neighborhood");
        entity.setCity("Old City");
        entity.setState("SP");
        entity.setIsActive(true);

        DeliveryAddressResponse response = new DeliveryAddressResponse(
                id, "87654321", "New Street", "456", "New Complement",
                "New Neighborhood", "New City", "RJ", null, null, Instant.now());

        when(deliveryAddressRepository.findActiveByIdAndUserId(id, userId)).thenReturn(Optional.of(entity));
        when(deliveryAddressRepository.save(entity)).thenReturn(entity);
        when(deliveryAddressMapper.toResponse(entity)).thenReturn(response);

        DeliveryAddressResponse result = deliveryAddressService.updateById(id, userId, request);

        assertNotNull(result);
        assertEquals("New Street", result.street());
        assertEquals("RJ", result.state());
    }

    @Test @DisplayName("Should throw DeliveryAddressNotFoundException if not found")
    void updateByIdTestCase2() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UpdateDeliveryAddressRequest request = new UpdateDeliveryAddressRequest(
                Optional.of("87654321"),
                Optional.of("New Street"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        when(deliveryAddressRepository.findActiveByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThrows(DeliveryAddressNotFoundException.class, () -> {
            deliveryAddressService.updateById(id, userId, request);
        });
    }
}