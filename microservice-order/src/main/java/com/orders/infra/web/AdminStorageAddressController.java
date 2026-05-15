package com.orders.infra.web;

import com.orders.application.dto.PagedResponse;
import com.orders.application.dto.StorageAddressRequest;
import com.orders.application.dto.StorageAddressResponse;
import com.orders.application.service.StorageAddressService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// TODO: integration tests with WebMvcTest
// TODO: openAPI docs
@RestController
@RequestMapping("/admin/api/v1/storage-addresses")
public class AdminStorageAddressController {
    private final StorageAddressService  storageAddressService;

    public AdminStorageAddressController(StorageAddressService storageAddressService) {
        this.storageAddressService = storageAddressService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<StorageAddressResponse>> getAllAddresses(Pageable pageable) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(storageAddressService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StorageAddressResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(storageAddressService.getById(id));
    }

    @PostMapping
    public ResponseEntity<StorageAddressResponse> create(
            @Valid @RequestBody StorageAddressRequest request,
            @RequestParam(defaultValue = "false") Boolean byCoordinates
    ) {
        StorageAddressResponse response;

        if (byCoordinates)
            response = storageAddressService.createByCoordinates(request);
        else
            response = storageAddressService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable UUID id
    ) {
        storageAddressService.deleteById(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
