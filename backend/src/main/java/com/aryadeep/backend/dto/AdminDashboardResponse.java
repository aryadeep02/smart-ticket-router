package com.aryadeep.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "AdminDashboardResponse",
        description = "System-wide ticket statistics available to administrators"
)
public record AdminDashboardResponse(

        @Schema(
                description = "Total number of tickets in the system",
                example = "250"
        )
        long totalTickets,

        @Schema(
                description = "Number of tickets currently open",
                example = "48"
        )
        long openTickets,

        @Schema(
                description = "Number of tickets currently in progress",
                example = "32"
        )
        long inProgressTickets,

        @Schema(
                description = "Number of resolved tickets",
                example = "150"
        )
        long resolvedTickets,

        @Schema(
                description = "Number of tickets that breached their SLA",
                example = "12"
        )
        long breachedTickets,

        @Schema(
                description = "Number of tickets that currently have no assigned agent",
                example = "18"
        )
        long unassignedTickets,

        @Schema(
                description = "Number of critical-priority tickets",
                example = "7"
        )
        long criticalTickets

) {
}