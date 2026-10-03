package com.jcommerce.web.form;

public record SignUpForm(
        String firstName,
        String lastName,
        String email,
        String password,
        String passwordConfirm
) {
}
