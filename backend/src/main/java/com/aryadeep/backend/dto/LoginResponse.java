package com.aryadeep.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "LoginResponse",
        description = "Response returned after successful authentication"
)
public record LoginResponse(

        @Schema(
                description = "Application JWT used to authenticate protected API requests",
                example = "eyJhbGciOiJIUzI1NiJ9...",
                format = "jwt"
        )
        String token,

        @Schema(
                description = "Unique identifier of the authenticated user",
                example = "202"
        )
        Long userId,

        @Schema(
                description = "Full name of the authenticated user",
                example = "Aryadeep Varshney"
        )
        String name,

        @Schema(
                description = "Email address of the authenticated user",
                example = "aryadeep@example.com"
        )
        String email,

        @Schema(
                description = "Role assigned to the authenticated user",
                example = "CUSTOMER"
        )
        String role

) {
}