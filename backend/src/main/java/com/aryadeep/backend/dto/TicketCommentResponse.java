package com.aryadeep.backend.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "TicketCommentResponse",
        description = "Comment attached to a support ticket"
)
public record TicketCommentResponse(

        @Schema(
                description = "Unique identifier of the comment",
                example = "87"
        )
        Long id,

        @Schema(
                description = "ID of the ticket containing the comment",
                example = "143"
        )
        Long ticketId,

        @Schema(
                description = "ID of the user who authored the comment",
                example = "42"
        )
        Long authorId,

        @Schema(
                description = "Name of the user who authored the comment",
                example = "John Doe"
        )
        String authorName,

        @Schema(
                description = "Content of the comment",
                example = "I have checked the transaction and it is still failing."
        )
        String content,

        @Schema(
                description = "Timestamp when the comment was created",
                example = "2026-09-20T14:30:00"
        )
        LocalDateTime createdAt

) {
}