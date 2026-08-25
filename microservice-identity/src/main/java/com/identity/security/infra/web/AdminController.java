package com.identity.security.infra.web;

import com.identity.security.application.dto.UpdateUserRole;
import com.identity.security.application.dto.UserCredentialsResponse;
import com.identity.security.application.service.AuthService;
import com.identity.security.application.service.mapper.UserCredentialsMapper;
import com.identity.security.docs.AdminControllerDocs;
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
@RequestMapping("/admin/api/v1")
public class AdminController implements AdminControllerDocs {
    private final AuthService authService;

    public AdminController(AuthService authService) {
        this.authService = authService;
    }

    @PatchMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUserRole(@PathVariable UUID userId, @Valid @RequestBody UpdateUserRole request) {
        authService.updateUserRole(userId, request.roles());

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StandardResponse<PagedResponse<UserCredentialsResponse>>> getAllUsers(Pageable pageable) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        StandardResponse
                                .success(authService.getAllUsers(pageable))
                                .build()
                );
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StandardResponse<UserCredentialsResponse>> getUserById(@PathVariable UUID userId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        StandardResponse
                                .success(authService.getUserById(userId))
                                .build()
                );
    }
}
