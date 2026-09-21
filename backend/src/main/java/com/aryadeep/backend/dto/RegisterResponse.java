package com.aryadeep.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RegisterResponse",
        description = "Response returned after creating a new customer account"
)
public record RegisterResponse(

        @Schema(
                description = "Unique identifier of the newly created user",
                example = "202"
        )
        Long id,

        @Schema(
                description = "Full name of the registered user",
                example = "Aryadeep Varshney"
        )
        String name,

        @Schema(
                description = "Email address associated with the account",
                example = "aryadeep@example.com"
        )
        String email,

        @Schema(
                description = "Role assigned to the user",
                example = "CUSTOMER"
        )
        String role,

        @Schema(
                description = "Whether the user's email address has been verified",
                example = "false"
        )
        boolean emailVerified,

        @Schema(
                description = "Whether the verification email was successfully sent",
                example = "true"
        )
        boolean verificationEmailSent,

        @Schema(
                description = "Human-readable result message",
                example = "Account created. Please check your email to verify your account."
        )
        String message

) {
}