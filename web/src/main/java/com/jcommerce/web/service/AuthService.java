package com.jcommerce.web.service;

import com.jcommerce.web.client.IdentityClient;
import com.jcommerce.web.share.dto.AuthSession;
import com.jcommerce.web.client.dto.LoginResponse;
import com.jcommerce.web.form.LoginForm;
import com.jcommerce.web.form.SignUpForm;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final IdentityClient identityClient;

    public AuthService(IdentityClient identityClient) {
        this.identityClient = identityClient;
    }

    public AuthSession login(LoginForm form){
        LoginResponse response = identityClient.login(form);

        return new AuthSession(
                response.data().accessToken().token(),
                response.data().refreshToken().token()
        );
    }

    public AuthSession signUp(SignUpForm form){
        identityClient.signUp(form);

        LoginResponse response = identityClient.login(
                new LoginForm(form.email(), form.password())
        );

        return new AuthSession(
                response.data().accessToken().token(),
                response.data().refreshToken().token()
        );
    }
}
