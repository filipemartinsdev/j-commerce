package com.orders.application.service;

import com.orders.application.dto.StorageAddressRequest;
import com.orders.application.dto.StorageAddressResponse;
import com.orders.application.exception.InvalidStorageAddressException;
import com.orders.application.exception.StorageAddressNotFoundException;
import com.orders.application.service.mapper.StorageAddressMapper;
import com.orders.domain.entity.StorageAddress;
import com.orders.infra.persistence.StorageAddressRepository;
import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.spring.PagedResponseFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StorageAddressService {
    private final StorageAddressRepository storageAddressRepository;
    private final StorageAddressMapper storageAddressMapper;
    private final GeocodingService geocodingService;

    public StorageAddressService(StorageAddressRepository storageAddressRepository, StorageAddressMapper storageAddressMapper, GeocodingService geocodingService) {
        this.storageAddressRepository = storageAddressRepository;
        this.storageAddressMapper = storageAddressMapper;
        this.geocodingService = geocodingService;
    }

    public Double[] getMainStorageAddressPoint(){
        var storageAddress = storageAddressRepository.findMainStorageAddress()
                .orElseThrow(() -> new StorageAddressNotFoundException("Any active storage address found"));

        Double[] point = new Double[2];
        point[0] = storageAddress.getLatitude();
        point[1] = storageAddress.getLongitude();

        return point;
    }

    public PagedResponse<StorageAddressResponse> getAll(Pageable pageable) {
        Page<StorageAddress> page = storageAddressRepository.findAllActive(pageable);

        return PagedResponseFactory.fromPage(page, storageAddressMapper::toResponse);
    }

    public StorageAddressResponse create(StorageAddressRequest request) {
        validateRequestToCreate(request);

        StorageAddress address = storageAddressMapper.toEntity(request);

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

        return storageAddressMapper.toResponse(storageAddressRepository.save(address));
    }

    private void validateRequestToCreate(StorageAddressRequest request) {
        if (request.haveNumber() && request.number().isEmpty())
            throw new InvalidStorageAddressException("Address number is mandatory");

        if (
                request.zipCode().isEmpty() ||
                        request.street().isEmpty() ||
                        request.neighborhood().isEmpty() ||
                        request.city().isEmpty() ||
                        request.state().isEmpty()
        ) {
            throw new InvalidStorageAddressException("Invalid delivery address");
        }
    }

    public StorageAddressResponse createByCoordinates(StorageAddressRequest request) {
        if (request.latitude().isEmpty() || request.longitude().isEmpty())
            throw new InvalidStorageAddressException("Latitude and Longitude is mandatory");


        GeocodingService.Address addressResponse = geocodingService.toAddress(
                new GeocodingService.Point(
                        request.latitude().get(),
                        request.longitude().get()
                )
        );

        if (!isAddressFromBrazil(addressResponse))
            throw new InvalidStorageAddressException("Address is not from Brazil");

        return storageAddressMapper.toResponse(registerAddressByCoordinatesResponse(request, addressResponse));
    }

    private boolean isAddressFromBrazil(GeocodingService.Address address) {
        return address.countryCode().equalsIgnoreCase("br");
    }

    private StorageAddress registerAddressByCoordinatesResponse(StorageAddressRequest request, GeocodingService.Address addressResponse) {
        StorageAddress address = storageAddressMapper.toEntity(addressResponse);

        address.setLatitude(request.latitude().get());
        address.setLongitude(request.longitude().get());
        address.setNumber("S/N");

        if (request.complement().isPresent())
            address.setComplement(request.complement().get());

        return storageAddressRepository.save(address);
    }


    public StorageAddressResponse getById(UUID id) {
        StorageAddress address = storageAddressRepository.findActiveById(id)
                .orElseThrow(() -> new StorageAddressNotFoundException("Storage address not found with ID: "+id));

        return storageAddressMapper.toResponse(address);
    }

    public void deleteById(UUID id) {
        StorageAddress address = storageAddressRepository.findActiveById(id)
                .orElseThrow(() -> new StorageAddressNotFoundException("Storage address not found with ID: "+id));
        address.setIsActive(false);

        storageAddressRepository.save(address);
    }
}
