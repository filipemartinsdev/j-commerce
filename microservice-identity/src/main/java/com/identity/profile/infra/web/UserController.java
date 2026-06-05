package com.identity.profile.infra.web;

import com.identity.profile.application.dto.UserProfileResponse;
import com.identity.profile.application.service.UserProfileService;
import com.identity.profile.docs.UserControllerDocs;
import io.github.responsekit.core.StandardResponse;
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
public class UserController implements UserControllerDocs {
    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    public ResponseEntity<StandardResponse<UserProfileResponse>> getAuthenticatedUser(@AuthenticationPrincipal Jwt authenticatedJWT) {
        UUID authenticatedUserId = UUID.fromString(authenticatedJWT.getSubject());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        StandardResponse
                                .success(userProfileService.getUserById(authenticatedUserId))
                                .build()
                );
    }
}
