package com.aryadeep.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        name = "RegisterRequest",
        description = "Request payload used to create a new customer account"
)
public record RegisterRequest(

        @Schema(
                description = "Full name of the user",
                example = "Aryadeep Varshney",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Name is required")
        String name,

        @Schema(
                description = "Email address to register",
                example = "aryadeep@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(
                description = "Password for the new account",
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
        String password

) {
}