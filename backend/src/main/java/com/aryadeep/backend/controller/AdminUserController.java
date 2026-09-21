package com.aryadeep.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aryadeep.backend.dto.AdminUserResponse;
import com.aryadeep.backend.dto.CreateUserRequest;
import com.aryadeep.backend.entity.User;
import com.aryadeep.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(
        name = "Admin Users",
        description = "Administrative user management operations"
)
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Create a user",
            description = "Creates a new customer or agent account. "
                    + "Agents must be assigned to a support team."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid user data or missing support team"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only admins can create users"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email is already registered"
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserResponse createUser(
            @Valid @RequestBody CreateUserRequest request) {

        User user = userService.createUserWithRole(
                request.name(),
                request.email(),
                request.password(),
                request.role(),
                request.supportTeamId());

        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getSupportTeam() != null
                        ? user.getSupportTeam().getId()
                        : null,
                user.getSupportTeam() != null
                        ? user.getSupportTeam().getName()
                        : null);
    }

    @Operation(
            summary = "Get all users",
            description = "Returns all registered users for administrative management."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only admins can access users"
            )
    })
    @GetMapping
    public List<AdminUserResponse> getAllUsers() {

        return userService.getAllUsers();
    }
}