package com.jcommerce.web.controller;

import com.jcommerce.web.form.LoginForm;
import com.jcommerce.web.form.SignUpForm;
import com.jcommerce.web.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String login(){
        return "auth/login";
    }

    @GetMapping("/sign-up")
    public String signUp(){
        return "auth/sign-up";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginForm form, HttpSession httpSession){
        var authSession = authService.login(form);
        httpSession.setAttribute("auth", authSession);
        return "redirect:/";
    }

    @PostMapping("/sign-up")
    public String signUp(@ModelAttribute SignUpForm form, HttpSession httpSession){
        var authSession = authService.signUp(form);
        httpSession.setAttribute("auth", authSession);
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpSession httpSession){
        httpSession.removeAttribute("auth");
        return "redirect:/";
    }
}
