package com.aryadeep.backend.dto;

import com.aryadeep.backend.entity.TicketStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateTicketStatusRequest(

        @NotNull(message = "Status is required")
        TicketStatus status

) {}