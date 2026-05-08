package com.identity.security.infra.web;

import com.identity.common.dto.PagedResponse;
import com.identity.common.dto.StandardResponse;
import com.identity.security.application.dto.UpdateUserRole;
import com.identity.security.application.dto.UserCredentialsResponse;
import com.identity.security.application.service.AuthService;
import com.identity.security.application.service.mapper.UserCredentialsMapper;
import com.identity.security.docs.AdminControllerDocs;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Void> updateUserRole(@PathVariable UUID userId, @Valid @RequestBody UpdateUserRole request) {
        authService.updateUserRole(userId, request.roles());

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @GetMapping("/users")
    public ResponseEntity<StandardResponse<PagedResponse<UserCredentialsResponse>>> getAllUsers(Pageable pageable) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(
                        authService.getAllUsers(pageable)
                ));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<StandardResponse<UserCredentialsResponse>> getUserById(@PathVariable UUID userId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(
                        authService.getUserById(userId)
                ));
    }
}
