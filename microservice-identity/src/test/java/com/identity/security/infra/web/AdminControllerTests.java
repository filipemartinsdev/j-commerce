package com.identity.security.infra.web;

import com.identity.common.dto.PagedResponse;
import com.identity.common.dto.StandardResponse;
import com.identity.config.SecurityConfig;
import com.identity.profile.application.dto.UserProfileResponse;
import com.identity.security.application.dto.UserCredentialsResponse;
import com.identity.security.application.exception.UserNotFoundException;
import com.identity.security.application.service.AuthService;
import com.identity.security.domain.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
public class AdminControllerTests {
    @Autowired private MockMvc mockMvc;

    @MockitoBean private AuthService authService;

    @Test @DisplayName("Should update user role and return status code 200")
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void updateUserRoleTestCase1() throws Exception {
        UUID userId = UUID.randomUUID();

        String requestBody = """
            {
                "roles": [
                    "ADMIN",
                    "STOCK_MANAGER"
                ]
            }
        """;

        doNothing().when(authService).updateUserRole(any(), any());

        mockMvc.perform(patch("/admin/api/v1/users/"+userId)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test @DisplayName("Should return response code 404 if user not exists")
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void updateUserRoleTestCase2() throws Exception {
        UUID userId = UUID.randomUUID();

        String requestBody = """
            {
                "roles": [
                    "ADMIN",
                    "STOCK_MANAGER"
                ]
            }
        """;

        doThrow(UserNotFoundException.class).when(authService).updateUserRole(any(), any());

        mockMvc.perform(patch("/admin/api/v1/users/"+userId)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }

    @Test @DisplayName("Should return response code 401 if client is not authenticated")
    void updateUserRoleTestCase3() throws Exception {
        UUID userId = UUID.randomUUID();

        String requestBody = """
            {
                "roles": [
                    "ADMIN",
                    "STOCK_MANAGER"
                ]
            }
        """;

        doNothing().when(authService).updateUserRole(any(), any());

        mockMvc.perform(patch("/admin/api/v1/users/"+userId)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }

    @Test @DisplayName("Should return response code 403 if client hasn't ADMIN authorities")
    @WithMockUser(authorities = "SCOPE_USER")
    void updateUserRoleTestCase4() throws Exception {
        UUID userId = UUID.randomUUID();

        String requestBody = """
            {
                "roles": [
                    "ADMIN",
                    "STOCK_MANAGER"
                ]
            }
        """;

        doNothing().when(authService).updateUserRole(any(), any());

        mockMvc.perform(patch("/admin/api/v1/users/"+userId)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden());
    }


    @Test @DisplayName("Should retrieve all users and return status code 200")
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void getAllUsersTestCase1() throws Exception {
        UUID userId = UUID.randomUUID();
        Instant userCreatedAt = Instant.now();

        PagedResponse<UserCredentialsResponse> authServiceResponse = PagedResponse.<UserCredentialsResponse>builder()
                .page(0)
                .size(20)
                .totalPages(1)
                .totalElements(1L)
                .isLast(true)
                .content(List.of(
                        new UserCredentialsResponse(
                                userId,
                                "testing@gmail.com",
                                "testing",
                                "testing",
                                List.of(Role.Value.USER),
                                userCreatedAt
                        )
                ))
                .build();

        String expectedResponse = """
            {
                "status": "success",
                "data": {
                    "page": 0,
                    "size": 20,
                    "totalPages": 1,
                    "totalElements": 1,
                    "isLast": true,
                    "content": [
                        {
                            "userId": "%s",
                            "email": "testing@gmail.com",
                            "firstName": "testing",
                            "lastName": "testing",
                            "roles": [
                                    "USER"
                            ],
                            "createdAt": "%s"
                        }
                    ]
                }
            }
        """.formatted(userId.toString(), userCreatedAt.toString());

        when(authService.getAllUsers(any()))
                .thenReturn(authServiceResponse);

        mockMvc.perform(get("/admin/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse));
    }

    @Test @DisplayName("Should return response code 401 if client is not authenticated")
    void getAllUsersTestCase2() throws Exception {
        mockMvc.perform(get("/admin/api/v1/users")
        ).andExpect(status().isUnauthorized());
    }

    @Test @DisplayName("Should return response code 403 if client hasn't ADMIN authorities")
    @WithMockUser(authorities = "SCOPE_USER")
    void getAllUsersTestCase3() throws Exception {
        mockMvc.perform(get("/admin/api/v1/users")
        ).andExpect(status().isForbidden());
    }


    @Test @DisplayName("Should retrieve user by ID and return response code 200")
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void getUserByIdTestCase1() throws Exception {
        UUID userId = UUID.randomUUID();
        Instant userCreatedAt = Instant.now();

        UserCredentialsResponse authServiceResponse = new UserCredentialsResponse(
                userId,
                "testing@gmail.com",
                "testing",
                "testing",
                List.of(Role.Value.USER),
                userCreatedAt
        );

        String expectedJSON = """
            {
                "status": "success",
                "data": {
                    "userId": "%s",
                    "email": "testing@gmail.com",
                    "firstName": "testing",
                    "lastName": "testing",
                    "roles": [
                        "USER"
                    ],
                    "createdAt": "%s"
                }
            }
        """.formatted(userId.toString(), userCreatedAt.toString());

        when(authService.getUserById(any()))
                .thenReturn(authServiceResponse);

        mockMvc.perform(get("/admin/api/v1/users/"+userId))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return response code 404 if user not exists")
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void getUserByIdTestCase2() throws Exception {
        UUID userId = UUID.randomUUID();

        doThrow(UserNotFoundException.class).when(authService).getUserById(any());

        mockMvc.perform(get("/admin/api/v1/users/"+userId))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("Should return response code 401 if client is not authenticated")
    void getUserByIdTestCase3() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/admin/api/v1/users/"+userId)
        ).andExpect(status().isUnauthorized());
    }

    @Test @DisplayName("Should return response code 403 if client hasn't ADMIN authorities")
    @WithMockUser(authorities = "SCOPE_USER")
    void getUserByIdTestCase4() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/admin/api/v1/users/"+userId)
        ).andExpect(status().isForbidden());
    }
}