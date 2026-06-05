package com.orders.application.service;

import com.orders.application.dto.StorageAddressRequest;
import com.orders.application.dto.StorageAddressResponse;
import com.orders.application.exception.InvalidStorageAddressException;
import com.orders.application.exception.StorageAddressNotFoundException;
import com.orders.application.service.mapper.StorageAddressMapper;
import com.orders.domain.entity.StorageAddress;
import com.orders.infra.persistence.StorageAddressRepository;
import io.github.responsekit.core.PagedResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StorageAddressServiceTests {
    @Mock private StorageAddressRepository storageAddressRepository;
    @Mock private StorageAddressMapper storageAddressMapper;
    @Mock private GeocodingService geocodingService;

    @InjectMocks private StorageAddressService storageAddressService;

    @Test @DisplayName("Should retrieve lat/lon points successfully")
    void getMainStorageAddressPointTestCase1() {
        StorageAddress entity = new StorageAddress();
        entity.setId(UUID.randomUUID());
        entity.setLatitude(-23.0);
        entity.setLongitude(-46.0);
        entity.setIsActive(true);

        when(storageAddressRepository.findMainStorageAddress()).thenReturn(Optional.of(entity));

        Double[] result = storageAddressService.getMainStorageAddressPoint();

        assertNotNull(result);
        assertEquals(-23.0, result[0]);
        assertEquals(-46.0, result[1]);
    }

    @Test @DisplayName("Should StorageAddressNotFoundException if not exists any active StorageAddress")
    void getMainStorageAddressPointTestCase2() {
        when(storageAddressRepository.findMainStorageAddress()).thenReturn(Optional.empty());

        assertThrows(StorageAddressNotFoundException.class, () -> {
            storageAddressService.getMainStorageAddressPoint();
        });
    }

    @Test @DisplayName("Should retrieve storage addresses successfully")
    void getAllTestCase1() {
        Pageable pageable = PageRequest.of(0, 10);

        StorageAddress entity = new StorageAddress();
        entity.setId(UUID.randomUUID());
        entity.setZipCode("12345678");
        entity.setStreet("Test Street");
        entity.setComplement("Complement");
        entity.setNeighborhood("Neighborhood");
        entity.setCity("City");
        entity.setState("SP");
        entity.setIsActive(true);

        Page<StorageAddress> page = new PageImpl<>(List.of(entity), pageable, 1);
        StorageAddressResponse response = new StorageAddressResponse(
                entity.getId(), "12345678", "Test Street", "Complement",
                "Neighborhood", "City", "SP", null, null, Instant.now());

        PagedResponse<StorageAddressResponse> pagedResponse = PagedResponse
                .content(List.of(response))
                .page(0).size(10).totalElements(1L).totalPages(1)
                .isLast(true).build();

        when(storageAddressRepository.findAllActive(pageable)).thenReturn(page);

        PagedResponse<StorageAddressResponse> result = storageAddressService.getAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.totalElements);
    }

    @Test @DisplayName("Should return empty PagedResponse if not exists any active StorageAddress")
    void getAllTestCase2() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<StorageAddress> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        PagedResponse<StorageAddressResponse> pagedResponse = PagedResponse
                .content(new ArrayList<StorageAddressResponse>())
                .page(0).size(10).totalElements(0L).totalPages(0)
                .isLast(true)
                .build();

        when(storageAddressRepository.findAllActive(pageable)).thenReturn(emptyPage);

        PagedResponse<StorageAddressResponse> result = storageAddressService.getAll(pageable);

        assertNotNull(result);
        assertEquals(0, result.totalElements);
        assertTrue(result.content.isEmpty());
    }

    @Test @DisplayName("Should create new StorageAddress successfully")
    void createTestCase1() {
        StorageAddressRequest request = new StorageAddressRequest(
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

        StorageAddress entity = new StorageAddress();
        entity.setId(UUID.randomUUID());
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

        StorageAddressResponse response = new StorageAddressResponse(
                entity.getId(), "12345678", "Test Street", "Complement",
                "Neighborhood", "City", "SP", -23.0, -46.0, Instant.now());

        when(storageAddressMapper.toEntity(request)).thenReturn(entity);
        when(geocodingService.toCoordinates(any())).thenReturn(new GeocodingService.Point(-23.0, -46.0));
        when(storageAddressRepository.save(entity)).thenReturn(entity);
        when(storageAddressMapper.toResponse(entity)).thenReturn(response);

        StorageAddressResponse result = storageAddressService.create(request);

        assertNotNull(result);
        assertEquals("12345678", result.zipCode());
        assertEquals("Test Street", result.street());
    }

    @Test @DisplayName("Should throw InvalidStorageAddressException if have any invalid field")
    void createTestCase2() {
        StorageAddressRequest request = new StorageAddressRequest(
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

        assertThrows(InvalidStorageAddressException.class, () -> {
            storageAddressService.create(request);
        });
    }

    @Test @DisplayName("Should create address by coordinates successfully")
    void createByCoordinatesTestCase1() {
        StorageAddressRequest request = new StorageAddressRequest(
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

        StorageAddress entity = new StorageAddress();
        entity.setId(UUID.randomUUID());
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

        StorageAddressResponse response = new StorageAddressResponse(
                entity.getId(), "12345678", "Street", "Complement",
                "Neighborhood", "City", "SP", -23.0, -46.0, Instant.now());

        when(geocodingService.toAddress(any())).thenReturn(geocodedAddress);
        when(storageAddressMapper.toEntity(geocodedAddress)).thenReturn(entity);
        when(storageAddressRepository.save(entity)).thenReturn(entity);
        when(storageAddressMapper.toResponse(entity)).thenReturn(response);

        StorageAddressResponse result = storageAddressService.createByCoordinates(request);

        assertNotNull(result);
        assertEquals("Street", result.street());
    }

    @Test @DisplayName("Should throw InvalidStorageAddressException if lat/lon are not present")
    void createByCoordinatesTestCase2() {
        StorageAddressRequest request = new StorageAddressRequest(
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

        assertThrows(InvalidStorageAddressException.class, () -> {
            storageAddressService.createByCoordinates(request);
        });
    }

    @Test @DisplayName("Should throw InvalidStorageAddressException if coordinates are not from Brazil")
    void createByCoordinatesTestCase3() {
        StorageAddressRequest request = new StorageAddressRequest(
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

        assertThrows(InvalidStorageAddressException.class, () -> {
            storageAddressService.createByCoordinates(request);
        });
    }

    @Test @DisplayName("Should retrieve address by ID successfully")
    void getByIdTestCase1() {
        UUID id = UUID.randomUUID();

        StorageAddress entity = new StorageAddress();
        entity.setId(id);
        entity.setZipCode("12345678");
        entity.setStreet("Test Street");
        entity.setComplement("Complement");
        entity.setNeighborhood("Neighborhood");
        entity.setCity("City");
        entity.setState("SP");
        entity.setIsActive(true);

        StorageAddressResponse response = new StorageAddressResponse(
                id, "12345678", "Test Street", "Complement",
                "Neighborhood", "City", "SP", null, null, Instant.now());

        when(storageAddressRepository.findActiveById(id)).thenReturn(Optional.of(entity));
        when(storageAddressMapper.toResponse(entity)).thenReturn(response);

        StorageAddressResponse result = storageAddressService.getById(id);

        assertNotNull(result);
        assertEquals(id, result.id());
        verify(storageAddressRepository).findActiveById(id);
    }

    @Test @DisplayName("Should throw StorageAddressNotFoundException if not found")
    void getByIdTestCase2() {
        UUID id = UUID.randomUUID();

        when(storageAddressRepository.findActiveById(id)).thenReturn(Optional.empty());

        assertThrows(StorageAddressNotFoundException.class, () -> {
            storageAddressService.getById(id);
        });
    }

    @Test @DisplayName("Should mark address as inactive successfully")
    void deleteByIdTestCase1() {
        UUID id = UUID.randomUUID();

        StorageAddress entity = new StorageAddress();
        entity.setId(id);
        entity.setIsActive(true);

        when(storageAddressRepository.findActiveById(id)).thenReturn(Optional.of(entity));
        when(storageAddressRepository.save(entity)).thenReturn(entity);

        storageAddressService.deleteById(id);

        assertFalse(entity.getIsActive());
        verify(storageAddressRepository).save(entity);
    }

    @Test @DisplayName("Should throw StorageAddressNotFoundException if not found")
    void deleteByIdTestCase2() {
        UUID id = UUID.randomUUID();

        when(storageAddressRepository.findActiveById(id)).thenReturn(Optional.empty());

        assertThrows(StorageAddressNotFoundException.class, () -> {
            storageAddressService.deleteById(id);
        });
    }
}