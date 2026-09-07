package com.aryadeep.backend.dto;

public record AdminDashboardResponse(
        long totalTickets,
        long openTickets,
        long inProgressTickets,
        long resolvedTickets,
        long breachedTickets,
        long unassignedTickets,
        long criticalTickets
) {}