package com.aryadeep.backend.dto;

import com.aryadeep.backend.entity.TicketStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "UpdateTicketStatusRequest",
        description = "Request used to change a ticket's workflow status"
)
public record UpdateTicketStatusRequest(

        @Schema(
                description = "New status requested for the ticket. "
                        + "The backend validates whether the transition is allowed.",
                example = "IN_PROGRESS",
                requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {
                        "OPEN",
                        "IN_PROGRESS",
                        "WAITING_FOR_CUSTOMER",
                        "RESOLVED",
                        "CLOSED"
                }
        )
        @NotNull(message = "Status is required")
        TicketStatus status

) {
}