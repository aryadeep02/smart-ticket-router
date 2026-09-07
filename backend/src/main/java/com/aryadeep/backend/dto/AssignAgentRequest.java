package com.aryadeep.backend.dto;

import jakarta.validation.constraints.NotNull;

public record AssignAgentRequest(

        @NotNull(message = "Agent ID is required")
        Long agentId

) {
}