package com.identity.security.infra.web;

import com.identity.config.SecurityConfig;
import com.identity.security.application.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(WellKnownController.class)
@Import(SecurityConfig.class)
public class WellKnownControllerTests {
    @MockitoBean private AuthService authService;

    @Autowired private MockMvc mockMvc;

    @Test @DisplayName("Should return the JWKS with status code 200 successfully")
    void getPublicJwksTestCase1() throws Exception {
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("example", "example");

        when(authService.getPublicJWKS())
                .thenReturn(expectedMap);

        String expectedJson = """
            {
                "example": "example"
            }
        """;

        mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }
}