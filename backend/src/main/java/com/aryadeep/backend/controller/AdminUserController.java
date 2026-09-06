package com.aryadeep.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aryadeep.backend.dto.CreateUserRequest;
import com.aryadeep.backend.dto.RegisterResponse;
import com.aryadeep.backend.entity.User;
import com.aryadeep.backend.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse createUser(
            @Valid @RequestBody CreateUserRequest request) {

        User user = userService.createUserWithRole(
                request.name(),
                request.email(),
                request.password(),
                request.role()
        );

        return new RegisterResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}