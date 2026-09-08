package com.aryadeep.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aryadeep.backend.dto.LoginRequest;
import com.aryadeep.backend.dto.LoginResponse;
import com.aryadeep.backend.dto.RegisterRequest;
import com.aryadeep.backend.dto.RegisterResponse;
import com.aryadeep.backend.service.EmailVerificationService;
import com.aryadeep.backend.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;

    public AuthController(
            UserService userService,
            EmailVerificationService emailVerificationService) {

        this.userService = userService;
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request) {

        return userService.registerUser(
                request.name(),
                request.email(),
                request.password());
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        return userService.loginUser(
                request.email(),
                request.password());
    }

    @GetMapping("/me")
    public LoginResponse getCurrentUser(
            Authentication authentication) {

        return userService.getCurrentUser(
                authentication.getName());
    }

    @GetMapping("/verify-email")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> verifyEmail(
            @RequestParam String token) {

        emailVerificationService.verifyEmail(token);

        return Map.of(
                "message",
                "Email verified successfully");
    }

    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> resendVerificationEmail(
            @RequestParam String email) {

        userService.resendVerificationEmail(email);

        return Map.of(
                "message",
                "If the account exists and is not verified, "
                        + "a new verification email has been sent.");
    }
}