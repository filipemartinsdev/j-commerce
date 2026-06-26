package com.orders.docs;

import com.orders.application.dto.StorageAddressRequest;
import com.orders.application.dto.StorageAddressResponse;

import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.core.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Admin Storage Address Management")
public interface AdminStorageAddressControllerDocs {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all storage addresses")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Storage addresses retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Client not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authenticated user can't perform this operation",
                    content = @Content
            )
    })
    ResponseEntity<StandardResponse<PagedResponse<StorageAddressResponse>>> getAllAddresses(Pageable pageable);

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get storage address by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Storage address retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Client not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authenticated user can't perform this operation",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Storage address not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<StorageAddressResponse>> getById(
            UUID id
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new storage address")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Storage address created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid storage address data or address is not from Brazil",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Client not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authenticated user can't perform this operation",
                    content = @Content
            )
    })
    ResponseEntity<StandardResponse<StorageAddressResponse>> create(
            StorageAddressRequest request,
            Boolean byCoordinates
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a storage address")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Storage address deleted successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Client not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authenticated user can't perform this operation",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Storage address not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<Void> deleteById(
            UUID id
    );
}
