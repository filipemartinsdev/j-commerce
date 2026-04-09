package com.identity.profile.infra.web;

import com.identity.common.dto.StandardResponse;
import com.identity.profile.application.service.JwtService;
import com.identity.profile.application.service.UserProfileService;
import com.identity.profile.application.dto.UserProfileResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserProfileService userProfileService;
    private final JwtService jwtService;

    public UserController(UserProfileService userProfileService, JwtService jwtService) {
        this.userProfileService = userProfileService;
        this.jwtService = jwtService;
    }

    @GetMapping("/me")
    public ResponseEntity<StandardResponse<UserProfileResponse>> getAuthenticatedUser(@AuthenticationPrincipal Jwt authenticatedJWT) {
        UUID authenticatedUserId = jwtService.getUserId(authenticatedJWT);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(
                    userProfileService.getUserById(authenticatedUserId))
            );
    }
}
