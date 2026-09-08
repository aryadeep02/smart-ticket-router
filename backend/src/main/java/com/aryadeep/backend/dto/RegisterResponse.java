package com.aryadeep.backend.dto;

public record RegisterResponse(

        Long id,

        String name,

        String email,

        String role,

        boolean emailVerified,

        boolean verificationEmailSent,

        String message

) {
}