package com.aryadeep.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aryadeep.backend.dto.LoginRequest;
import com.aryadeep.backend.dto.LoginResponse;
import com.aryadeep.backend.dto.RegisterRequest;
import com.aryadeep.backend.dto.RegisterResponse;
import com.aryadeep.backend.entity.User;
import com.aryadeep.backend.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {

        User user = userService.registerUser(
                request.name(),
                request.email(),
                request.password());

        return new RegisterResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name());
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {

        return userService.loginUser(
                request.email(),
                request.password());
    }

}