package com.identity.profile.infra.web;

import com.identity.config.SecurityConfig;
import com.identity.profile.application.dto.UserProfileResponse;
import com.identity.profile.application.exception.UserProfileNotFoundException;
import com.identity.profile.application.service.UserProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTests {
    @Autowired private MockMvc mockMvc;

    @MockitoBean private UserProfileService userProfileService;

    @Test @DisplayName("Should return user profile and status code 200")
    @WithMockUser(roles = "USER")
    void getAuthenticatedUserTestCase1() throws Exception {
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        UserProfileResponse response = new UserProfileResponse(
                userId,
                "test@example.com",
                "John",
                "Doe",
                createdAt
        );

        String expectedJSON = """
            {
                "status": "success",
                "data": {
                    "userId": "%s",
                    "email": "test@example.com",
                    "firstName": "John",
                    "lastName": "Doe",
                    "createdAt": "%s"
                }
            }
            """.formatted(userId.toString(), createdAt.toString());

        when(userProfileService.getUserById(userId))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/users/me")
                        .with(jwt()
                                .jwt(jwt -> jwt.subject(userId.toString())))
                )
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 404 if user profile not found")
    @WithMockUser(roles = "USER")
    void getAuthenticatedUserTestCase2() throws Exception {
        UUID userId = UUID.randomUUID();

        doThrow(UserProfileNotFoundException.class).when(userProfileService).getUserById(userId);

        mockMvc.perform(get("/api/v1/users/me")
                        .with(jwt()
                                .jwt(jwt -> jwt.subject(userId.toString())))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return response code 401 if client is not authenticated")
    void getAuthenticatedUserTestCase3() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }
}