package com.aryadeep.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        name = "ResetPasswordRequest",
        description = "Request used to reset a user's password using a valid reset token"
)
public record ResetPasswordRequest(

        @Schema(
                description = "Secure token received through the password reset email",
                example = "reset-token-from-email",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Reset token is required")
        String token,

        @Schema(
                description = "New password for the account",
                example = "NewPassword@123",
                requiredMode = Schema.RequiredMode.REQUIRED,
                format = "password",
                minLength = 8,
                maxLength = 100
        )
        @NotBlank(message = "New password is required")
        @Size(
                min = 8,
                max = 100,
                message = "Password must be between 8 and 100 characters"
        )
        String newPassword

) {}