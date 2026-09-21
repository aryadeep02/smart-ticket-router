package com.aryadeep.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aryadeep.backend.dto.AdminDashboardResponse;
import com.aryadeep.backend.repository.TicketRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@Tag(
        name = "Admin Dashboard",
        description = "Administrative ticket statistics"
)
@SecurityRequirement(name = "bearerAuth")
public class AdminDashboardController {

    private final TicketRepository ticketRepository;

    public AdminDashboardController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Operation(
            summary = "Get admin dashboard statistics",
            description = "Returns aggregated ticket statistics for administrators."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Dashboard statistics retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only admins can access the dashboard"
            )
    })
    @GetMapping
    public AdminDashboardResponse getDashboard() {

        return new AdminDashboardResponse(
                ticketRepository.count(),
                ticketRepository.countByStatus(
                        com.aryadeep.backend.entity.TicketStatus.OPEN),
                ticketRepository.countByStatus(
                        com.aryadeep.backend.entity.TicketStatus.IN_PROGRESS),
                ticketRepository.countByStatus(
                        com.aryadeep.backend.entity.TicketStatus.RESOLVED),
                ticketRepository.countBySlaBreachedTrue(),
                ticketRepository.countByAssignedAgentIsNull(),
                ticketRepository.countByPriority(
                        com.aryadeep.backend.entity.TicketPriority.CRITICAL)
        );
    }
}