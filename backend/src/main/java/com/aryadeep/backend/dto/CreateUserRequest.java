package com.aryadeep.backend.dto;

import com.aryadeep.backend.entity.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(
        name = "CreateUserRequest",
        description = "Request used by an administrator to create a customer or agent account"
)
public record CreateUserRequest(

        @Schema(
                description = "Full name of the user",
                example = "John Doe",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Name is required")
        String name,

        @Schema(
                description = "Email address for the new account",
                example = "john@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(
                description = "Initial password for the account",
                example = "Password@123",
                requiredMode = Schema.RequiredMode.REQUIRED,
                format = "password",
                minLength = 8
        )
        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                message = "Password must be at least 8 characters"
        )
        String password,

        @Schema(
                description = "Role assigned to the new user",
                example = "AGENT",
                requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {
                        "CUSTOMER",
                        "AGENT",
                        "ADMIN"
                }
        )
        @NotNull(message = "Role is required")
        Role role,

        @Schema(
                description = "ID of the support team assigned to the user. Required when creating an AGENT.",
                example = "2",
                nullable = true
        )
        Long supportTeamId

) {
}