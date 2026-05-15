package com.orders.application.service;

import com.orders.application.dto.AddressByCoordinatesResponse;
import com.orders.application.dto.PagedResponse;
import com.orders.application.dto.StorageAddressRequest;
import com.orders.application.dto.StorageAddressResponse;
import com.orders.application.exception.InvalidDeliveryAddressCoordinatesException;
import com.orders.application.exception.InvalidStorageAddressException;
import com.orders.application.exception.StorageAddressNotFoundException;
import com.orders.application.factory.PagedResponseFactory;
import com.orders.application.service.mapper.StorageAddressMapper;
import com.orders.domain.entity.StorageAddress;
import com.orders.infra.persistence.StorageAddressRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

// TODO: create unit tests for this

@Service
public class StorageAddressService {
    private final StorageAddressRepository storageAddressRepository;
    private final PagedResponseFactory<StorageAddressResponse> pagedResponseFactory;
    private final StorageAddressMapper storageAddressMapper;
    private final NominatimClient nominatimClient;

    public StorageAddressService(StorageAddressRepository storageAddressRepository, PagedResponseFactory<StorageAddressResponse> pagedResponseFactory, StorageAddressMapper storageAddressMapper, NominatimClient nominatimClient) {
        this.storageAddressRepository = storageAddressRepository;
        this.pagedResponseFactory = pagedResponseFactory;
        this.storageAddressMapper = storageAddressMapper;
        this.nominatimClient = nominatimClient;
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

        return pagedResponseFactory.fromPage(page, storageAddressMapper::toResponse);
    }

    public StorageAddressResponse create(StorageAddressRequest request) {
        validateRequestToCreate(request);

        StorageAddress address = storageAddressMapper.toEntity(request);

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
            throw new InvalidDeliveryAddressCoordinatesException("Latitude and Longitude is mandatory");

        AddressByCoordinatesResponse addressResponse = requestAddress(
                request.latitude().get(),
                request.longitude().get());

        if (!isAddressFromBrazil(addressResponse))
            throw new InvalidDeliveryAddressCoordinatesException("Address is not from Brazil");

        if (addressResponse.address().zipCode() == null || addressResponse.address().road() == null)
            throw new InvalidDeliveryAddressCoordinatesException("Invalid address");

        return storageAddressMapper.toResponse(registerAddressByCoordinatesResponse(request, addressResponse));
    }

    private AddressByCoordinatesResponse requestAddress(double lat, double lon){
        ResponseEntity<AddressByCoordinatesResponse> response = nominatimClient.getAddressByCoordinates(
                lat, lon, "json"
        );

        return response.getBody();
    }

    private boolean isAddressFromBrazil(AddressByCoordinatesResponse response) {
        return response.address().countryCode().equals("br");
    }

    private StorageAddress registerAddressByCoordinatesResponse(StorageAddressRequest request, AddressByCoordinatesResponse addressByCoordinatesResponse) {
        StorageAddress address = storageAddressMapper.toEntity(addressByCoordinatesResponse);

        address.setLatitude(request.latitude().get());
        address.setLongitude(request.longitude().get());

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
