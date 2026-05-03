package com.identity.security.infra.web;

import com.identity.security.application.service.AuthService;
import com.identity.security.docs.WellKnownControllerDocs;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/.well-known")
public class WellKnownController implements WellKnownControllerDocs {
    private final AuthService authService;

    public WellKnownController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/jwks.json")
    public ResponseEntity<Map<String, Object>> getPublicJwks() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.getPublicJWKS());
    }
}
