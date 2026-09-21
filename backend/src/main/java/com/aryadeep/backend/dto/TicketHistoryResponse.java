package com.aryadeep.backend.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "TicketHistoryResponse",
        description = "Audit-history entry representing a change made to a ticket"
)
public record TicketHistoryResponse(

        @Schema(
                description = "Unique identifier of the history entry",
                example = "101"
        )
        Long id,

        @Schema(
                description = "ID of the ticket that was changed",
                example = "143"
        )
        Long ticketId,

        @Schema(
                description = "ID of the user who made the change",
                example = "42"
        )
        Long changedByUserId,

        @Schema(
                description = "Type of action performed",
                example = "STATUS_CHANGED"
        )
        String action,

        @Schema(
                description = "Previous value before the change",
                example = "OPEN",
                nullable = true
        )
        String oldValue,

        @Schema(
                description = "New value after the change",
                example = "IN_PROGRESS",
                nullable = true
        )
        String newValue,

        @Schema(
                description = "Timestamp when the change occurred",
                example = "2026-09-20T14:30:00"
        )
        LocalDateTime createdAt

) {
}