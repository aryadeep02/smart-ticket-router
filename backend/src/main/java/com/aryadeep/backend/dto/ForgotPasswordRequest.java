package com.aryadeep.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(
        name = "ForgotPasswordRequest",
        description = "Request used to initiate the password reset process"
)
public record ForgotPasswordRequest(

        @Schema(
                description = "Email address associated with the account",
                example = "user@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        String email

) {}