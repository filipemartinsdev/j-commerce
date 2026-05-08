package com.identity.security.infra.web;

import com.identity.config.SecurityConfig;
import com.identity.security.application.dto.LoginResponse;
import com.identity.security.application.dto.TokenResponse;
import com.identity.security.application.exception.UserAlreadyExistsException;
import com.identity.security.application.exception.UserNotFoundException;
import com.identity.security.application.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTests {
    @MockitoBean private AuthService authService;

    @Autowired private MockMvc mockMvc;

    @Test @DisplayName("Should register user and return status code 201")
    void registerTestCase1() throws Exception {
        String requestBody = """
            {
                "email": "testing@gmail.com",
                "password": "testing123",
                "firstName": "testing",
                "lastName": "testing"
            }
        """;

        doNothing().when(authService).register(any());

        mockMvc.perform(post("/api/v1/auth/register")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated());
    }

    @Test @DisplayName("Should return status code 400 if user already exists by email")
    void registerTestCase2() throws Exception {
        String requestBody = """
            {
                "email": "testing@gmail.com",
                "password": "testing123",
                "firstName": "testing",
                "lastName": "testing"
            }
        """;

        doThrow(UserAlreadyExistsException.class).when(authService).register(any());

        mockMvc.perform(post("/api/v1/auth/register")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test @DisplayName("Should login and return status code 200")
    void loginTestCase1() throws Exception {
        String requestBody = """
            {
                "email": "testing@gmail.com",
                "password": "testing123"
            }
        """;

        var loginResponse = new LoginResponse(
                new TokenResponse("access token", Instant.now()),
                new TokenResponse("refresh token", Instant.now())
        );

        String expectedJSON =
                """
            {
                "status": "success",
                "data": {
                    "accessToken": {
                        "token": "access token",
                        "expiration": "%s"
                    },
                    "refreshToken": {
                        "token": "refresh token",
                        "expiration": "%s"
                    }
                }
            }
        """.formatted(loginResponse.accessToken().expiration().toString(), loginResponse.refreshToken().expiration().toString());

        when(authService.login(any()))
                .thenReturn(loginResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 404 if user not exists")
    void loginTestCase2() throws Exception {
        String requestBody = """
            {
                "email": "testing@gmail.com",
                "password": "testing123"
            }
        """;

        doThrow(UserNotFoundException.class).when(authService).login(any());

        mockMvc.perform(post("/api/v1/auth/login")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }

    @Test @DisplayName("Should refresh and return status code 200")
    void refreshTestCase1() throws Exception {
        String requestBody = """
            {
                "refreshToken": "token"
            }
        """;

        var loginResponse = new LoginResponse(
                new TokenResponse("access token", Instant.now()),
                new TokenResponse("refresh token", Instant.now())
        );

        String expectedJSON = """
            {
                "status": "success",
                "data": {
                    "accessToken": {
                        "token": "access token",
                        "expiration": "%s"
                    },
                    "refreshToken": {
                        "token": "refresh token",
                        "expiration": "%s"
                    }
                }
            }
        """.formatted(loginResponse.accessToken().expiration().toString(), loginResponse.refreshToken().expiration().toString());

        when(authService.refresh(any()))
                .thenReturn(loginResponse);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 400 if refresh token is invalid")
    void refreshTestCase2() throws Exception {
        String requestBody = """
            {
                "refreshToken": "token"
            }
        """;

        doThrow(BadJwtException.class).when(authService).refresh(any());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest());
    }
}