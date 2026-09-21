package com.aryadeep.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "AssignAgentRequest",
        description = "Request used by an administrator to assign an agent to a ticket"
)
public record AssignAgentRequest(

        @Schema(
                description = "ID of the agent to assign to the ticket",
                example = "42",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Agent ID is required")
        Long agentId

) {
}