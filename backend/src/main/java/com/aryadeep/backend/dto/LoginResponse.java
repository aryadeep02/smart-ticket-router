package com.aryadeep.backend.dto;
public record LoginResponse(
        String token,
        Long userId,
        String name,
        String email,
        String role
) {}