package com.jcommerce.web.client;

import com.jcommerce.web.client.dto.LoginRequest;
import com.jcommerce.web.client.dto.LoginResponse;
import com.jcommerce.web.client.dto.RegisterRequest;
import com.jcommerce.web.form.LoginForm;
import com.jcommerce.web.form.SignUpForm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class IdentityClientImpl implements IdentityClient {
    @Value("${app.services.identity.url}")
    private String SERVICE_BASE_URL;

    private final RestClient restClientBuilder = RestClient.create();


    @Override
    public LoginResponse login(LoginForm form) {
        var response = restClientBuilder.post()
                .uri(SERVICE_BASE_URL + "/api/v1/auth/login")
                .body(toLoginRequest(form))
                .retrieve()
                .toEntity(LoginResponse.class);

        if (!response.getStatusCode().is2xxSuccessful())
            throw new RuntimeException();

        return response.getBody();
    }

    private LoginRequest toLoginRequest(LoginForm form) {
        return new LoginRequest(form.email(), form.password());
    }


    @Override
    public void signUp(SignUpForm form) {
        var response = restClientBuilder.post()
                .uri(SERVICE_BASE_URL + "/api/v1/auth/register")
                .body(toRegisterRequest(form))
                .retrieve()
                .toEntity(LoginResponse.class);

        if (!response.getStatusCode().is2xxSuccessful())
            throw new RuntimeException();
    }

    private RegisterRequest toRegisterRequest(SignUpForm form) {
        return new RegisterRequest(
                form.email(),
                form.password(),
                form.firstName(),
                form.lastName()
        );
    }
}
