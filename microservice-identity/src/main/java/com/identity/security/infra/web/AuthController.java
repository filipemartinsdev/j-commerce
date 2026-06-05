package com.identity.security.infra.web;

import com.identity.security.application.dto.LoginRequest;
import com.identity.security.application.dto.LoginResponse;
import com.identity.security.application.dto.RefreshRequest;
import com.identity.security.application.dto.RegisterRequest;
import com.identity.security.application.service.AuthService;
import com.identity.security.docs.AuthControllerDocs;
import io.github.responsekit.core.StandardResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthControllerDocs {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<StandardResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        StandardResponse.success(
                                authService.login(request)
                        ).build()
                );
    }

    @PostMapping("/refresh")
    public ResponseEntity<StandardResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshRequest request){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        StandardResponse.success(
                                authService.refresh(request)
                        ).build()
                );
    }
}
