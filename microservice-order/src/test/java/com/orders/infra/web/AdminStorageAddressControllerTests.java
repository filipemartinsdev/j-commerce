package com.orders.infra.web;

import com.orders.application.dto.StorageAddressRequest;
import com.orders.application.dto.StorageAddressResponse;
import com.orders.application.service.StorageAddressService;
import com.orders.config.SecurityConfig;
import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.core.StandardResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminStorageAddressController.class)
@Import(SecurityConfig.class)
public class AdminStorageAddressControllerTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private StorageAddressService storageAddressService;

    @WithMockUser(roles = "ADMIN")
    @Test @DisplayName("Should retrieve addresses and return status code 200")
    void getAllAddressesTestCase1() throws Exception {
        UUID addressId = UUID.randomUUID();

        var response = new StorageAddressResponse(
                addressId,
                "12345-678",
                "Main Street",
                "Apt 1",
                "Downtown",
                "São Paulo",
                "SP",
                -23.5505,
                -46.6333,
                Instant.now()
        );

        PagedResponse<StorageAddressResponse> pagedResponse = PagedResponse
                .content(List.of(response))
                .page(0).size(20).totalElements(1L).totalPages(1)
                .isLast(true)
                .build();

        when(storageAddressService.getAll(any())).thenReturn(pagedResponse);

        String expectedJSON = objectMapper.writeValueAsString(StandardResponse.success(pagedResponse).build());

        mockMvc.perform(get("/admin/api/v1/storage-addresses"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void getAllAddressesTestCase2() throws Exception {
        mockMvc.perform(get("/admin/api/v1/storage-addresses"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(roles = "USER")
    @Test @DisplayName("Should return status code 403 if client role is not DRIVER, LOGISTICS or ADMIN")
    void getAllAddressesTestCase3() throws Exception {
        mockMvc.perform(get("/admin/api/v1/storage-addresses"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "LOGISTICS")
    @Test @DisplayName("Should address and return status code 200")
    void getByIdTestCase1() throws Exception {
        UUID addressId = UUID.randomUUID();

        var response = new StorageAddressResponse(
                addressId,
                "12345-678",
                "Main Street",
                "Apt 1",
                "Downtown",
                "São Paulo",
                "SP",
                -23.5505,
                -46.6333,
                Instant.now()
        );

        when(storageAddressService.getById(addressId)).thenReturn(response);

        String expectedJSON = objectMapper.writeValueAsString(StandardResponse.success(response).build());

        mockMvc.perform(get("/admin/api/v1/storage-addresses/{id}", addressId))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void getByIdTestCase2() throws Exception {
        UUID addressId = UUID.randomUUID();

        mockMvc.perform(get("/admin/api/v1/storage-addresses/{id}", addressId))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(roles = "USER")
    @Test @DisplayName("Should return status code 403 if client role is not DRIVER, LOGISTICS or ADMIN")
    void getByIdTestCase3() throws Exception {
        UUID addressId = UUID.randomUUID();

        mockMvc.perform(get("/admin/api/v1/storage-addresses/{id}", addressId))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "ADMIN")
    @Test @DisplayName("Should address and return status code 200")
    void createTestCase1() throws Exception {
        UUID addressId = UUID.randomUUID();

        var request = new StorageAddressRequest(
                true,
                Optional.of("12345-678"),
                Optional.of("Main Street"),
                Optional.of("100"),
                Optional.of("Apt 1"),
                Optional.of("Downtown"),
                Optional.of("São Paulo"),
                Optional.of("SP"),
                Optional.empty(),
                Optional.empty()
        );

        var response = new StorageAddressResponse(
                addressId,
                "12345-678",
                "Main Street 100",
                "Apt 1",
                "Downtown",
                "São Paulo",
                "SP",
                null,
                null,
                Instant.now()
        );

        when(storageAddressService.create(any(StorageAddressRequest.class))).thenReturn(response);

        String requestBody = objectMapper.writeValueAsString(request);
        String expectedJSON = objectMapper.writeValueAsString(StandardResponse.success(response).build());

        mockMvc.perform(post("/admin/api/v1/storage-addresses")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isCreated())
            .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void createTestCase2() throws Exception {
        var request = new StorageAddressRequest(
                true,
                Optional.of("12345-678"),
                Optional.of("Main Street"),
                Optional.of("100"),
                Optional.of("Apt 1"),
                Optional.of("Downtown"),
                Optional.of("São Paulo"),
                Optional.of("SP"),
                Optional.empty(),
                Optional.empty()
        );

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/admin/api/v1/storage-addresses")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isUnauthorized());
    }

    @WithMockUser(roles = "DRIVER")
    @Test @DisplayName("Should return status code 403 if client role is not LOGISTICS or ADMIN")
    void createTestCase3() throws Exception {
        var request = new StorageAddressRequest(
                true,
                Optional.of("12345-678"),
                Optional.of("Main Street"),
                Optional.of("100"),
                Optional.of("Apt 1"),
                Optional.of("Downtown"),
                Optional.of("São Paulo"),
                Optional.of("SP"),
                Optional.empty(),
                Optional.empty()
        );

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/admin/api/v1/storage-addresses")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "ADMIN")
    @Test @DisplayName("Should delete by ID and return status code 200")
    void deleteByIdTestCase1() throws Exception {
        UUID addressId = UUID.randomUUID();

        doNothing().when(storageAddressService).deleteById(addressId);

        mockMvc.perform(delete("/admin/api/v1/storage-addresses/{id}", addressId))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void deleteByIdTestCase2() throws Exception {
        UUID addressId = UUID.randomUUID();

        mockMvc.perform(delete("/admin/api/v1/storage-addresses/{id}", addressId))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(roles = "DRIVER")
    @Test @DisplayName("Should return status code 403 if client role is not LOGISTICS or ADMIN")
    void deleteByIdTestCase3() throws Exception {
        UUID addressId = UUID.randomUUID();

        mockMvc.perform(delete("/admin/api/v1/storage-addresses/{id}", addressId))
                .andExpect(status().isForbidden());
    }
}