package com.identity.security.docs;

import com.nimbusds.jose.jwk.JWKSet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Tag(name = "Well Known")
public interface WellKnownControllerDocs {
    @Operation(
            summary = "Get public JWKS"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Public JWKS retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = JWKSet.class
                            )
                    )
            )
    })
    ResponseEntity<Map<String, Object>> getPublicJwks();
}
