package com.aryadeep.backend.dto;

public record AdminUserResponse(
        Long id,
        String name,
        String email,
        String role,
        Long supportTeamId,
        String supportTeamName
) {}