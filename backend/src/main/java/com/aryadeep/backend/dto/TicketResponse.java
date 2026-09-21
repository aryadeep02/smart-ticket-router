package com.aryadeep.backend.dto;

import java.time.LocalDateTime;

import com.aryadeep.backend.entity.TicketCategory;
import com.aryadeep.backend.entity.TicketPriority;
import com.aryadeep.backend.entity.TicketStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "TicketResponse",
        description = "Complete support-ticket representation returned by the API"
)
public record TicketResponse(

        @Schema(
                description = "Unique identifier of the ticket",
                example = "143"
        )
        Long id,

        @Schema(
                description = "Short title describing the issue",
                example = "Payment failed"
        )
        String title,

        @Schema(
                description = "Detailed description of the issue",
                example = "My payment was declined while checking out."
        )
        String description,

        @Schema(
                description = "AI-assigned ticket category",
                example = "PAYMENT"
        )
        TicketCategory category,

        @Schema(
                description = "Ticket priority",
                example = "HIGH"
        )
        TicketPriority priority,

        @Schema(
                description = "Current ticket workflow status",
                example = "OPEN"
        )
        TicketStatus status,

        @Schema(
                description = "ID of the customer who created the ticket",
                example = "202"
        )
        Long customerId,

        @Schema(
                description = "ID of the agent currently assigned to the ticket",
                example = "42",
                nullable = true
        )
        Long assignedAgentId,

        @Schema(
                description = "ID of the support team responsible for the ticket",
                example = "2",
                nullable = true
        )
        Long supportTeamId,

        @Schema(
                description = "Confidence returned by the AI classification service",
                example = "0.92",
                nullable = true,
                minimum = "0.0",
                maximum = "1.0"
        )
        Double aiConfidence,

        @Schema(
                description = "AI classification state",
                example = "COMPLETED"
        )
        String aiClassificationStatus,

        @Schema(
                description = "SLA deadline calculated from ticket priority",
                example = "2026-09-20T18:30:00",
                nullable = true
        )
        LocalDateTime slaDeadline,

        @Schema(
                description = "Whether the ticket has breached its SLA",
                example = "false"
        )
        Boolean slaBreached,

        @Schema(
                description = "Time when the ticket was resolved",
                example = "2026-09-20T17:10:00",
                nullable = true
        )
        LocalDateTime resolvedAt,

        @Schema(
                description = "Timestamp when the ticket was created",
                example = "2026-09-20T14:30:00"
        )
        LocalDateTime createdAt,

        @Schema(
                description = "Timestamp when the ticket was last updated",
                example = "2026-09-20T15:10:00"
        )
        LocalDateTime updatedAt

) {
}