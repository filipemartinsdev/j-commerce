package com.jcommerce.web.client;

import com.jcommerce.web.client.dto.LoginResponse;
import com.jcommerce.web.form.LoginForm;
import com.jcommerce.web.form.SignUpForm;

public interface IdentityClient {
    LoginResponse login(LoginForm form);

    void signUp(SignUpForm form);
}
