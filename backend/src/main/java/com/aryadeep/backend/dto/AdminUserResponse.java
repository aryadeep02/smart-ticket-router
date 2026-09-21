package com.aryadeep.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "AdminUserResponse",
        description = "User information returned for administrative management"
)
public record AdminUserResponse(

        @Schema(
                description = "Unique identifier of the user",
                example = "42"
        )
        Long id,

        @Schema(
                description = "Full name of the user",
                example = "John Doe"
        )
        String name,

        @Schema(
                description = "Email address of the user",
                example = "john@example.com"
        )
        String email,

        @Schema(
                description = "Role assigned to the user",
                example = "AGENT"
        )
        String role,

        @Schema(
                description = "ID of the support team assigned to the user",
                example = "2",
                nullable = true
        )
        Long supportTeamId,

        @Schema(
                description = "Name of the support team assigned to the user",
                example = "PAYMENT_SUPPORT",
                nullable = true
        )
        String supportTeamName

) {
}