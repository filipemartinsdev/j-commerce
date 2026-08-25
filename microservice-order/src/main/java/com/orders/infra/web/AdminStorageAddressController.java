package com.orders.infra.web;

import com.orders.application.dto.StorageAddressRequest;
import com.orders.application.dto.StorageAddressResponse;
import com.orders.application.service.StorageAddressService;
import com.orders.docs.AdminStorageAddressControllerDocs;

import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.core.StandardResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/storage-addresses")
public class AdminStorageAddressController implements AdminStorageAddressControllerDocs{
    private final StorageAddressService  storageAddressService;

    public AdminStorageAddressController(StorageAddressService storageAddressService) {
        this.storageAddressService = storageAddressService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LOGISTICS', 'ADMIN')")
    public ResponseEntity<StandardResponse<PagedResponse<StorageAddressResponse>>> getAllAddresses(Pageable pageable) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(storageAddressService.getAll(pageable)).build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LOGISTICS', 'ADMIN')")
    public ResponseEntity<StandardResponse<StorageAddressResponse>> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(storageAddressService.getById(id)).build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LOGISTICS', 'ADMIN')")
    public ResponseEntity<StandardResponse<StorageAddressResponse>> create(
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
                .body(StandardResponse.success(response).build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LOGISTICS', 'ADMIN')")
    public ResponseEntity<Void> deleteById(
            @PathVariable UUID id
    ) {
        storageAddressService.deleteById(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
