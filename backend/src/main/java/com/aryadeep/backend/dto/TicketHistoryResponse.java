package com.aryadeep.backend.dto;

import java.time.LocalDateTime;

public record TicketHistoryResponse(
        Long id,
        Long ticketId,
        Long changedByUserId,
        String action,
        String oldValue,
        String newValue,
        LocalDateTime createdAt
) {
}