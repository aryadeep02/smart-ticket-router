package com.aryadeep.backend.dto;

import java.time.LocalDateTime;

import com.aryadeep.backend.entity.TicketCategory;
import com.aryadeep.backend.entity.TicketPriority;
import com.aryadeep.backend.entity.TicketStatus;

public record TicketResponse(
        Long id,
        String title,
        String description,
        TicketCategory category,
        TicketPriority priority,
        TicketStatus status,
        Long customerId,
        Long assignedAgentId,
        Long supportTeamId,
        Double aiConfidence,
        String aiClassificationStatus,
        LocalDateTime slaDeadline,
        Boolean slaBreached,
        LocalDateTime resolvedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}