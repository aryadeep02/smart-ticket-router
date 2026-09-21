package com.aryadeep.backend.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aryadeep.backend.dto.AdminDashboardResponse;
import com.aryadeep.backend.dto.AssignAgentRequest;
import com.aryadeep.backend.dto.CreateTicketRequest;
import com.aryadeep.backend.dto.TicketHistoryResponse;
import com.aryadeep.backend.dto.TicketResponse;
import com.aryadeep.backend.dto.UpdateTicketStatusRequest;
import com.aryadeep.backend.entity.TicketCategory;
import com.aryadeep.backend.entity.TicketPriority;
import com.aryadeep.backend.entity.TicketStatus;
import com.aryadeep.backend.service.TicketHistoryService;
import com.aryadeep.backend.service.TicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(
        name = "Tickets",
        description = "Ticket creation, retrieval, filtering, assignment, workflow and history operations")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

    private final TicketService ticketService;
    private final TicketHistoryService ticketHistoryService;

    public TicketController(
            TicketService ticketService,
            TicketHistoryService ticketHistoryService) {

        this.ticketService = ticketService;
        this.ticketHistoryService = ticketHistoryService;
    }

    @Operation(
            summary = "Create a new ticket",
            description = "Creates a support ticket for the authenticated customer or admin. "
                    + "The backend performs AI classification, priority assignment, "
                    + "support-team routing and SLA calculation."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Ticket created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid ticket data"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "User does not have permission to create tickets"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            Authentication authentication) {

        return ticketService.createTicket(
                request,
                authentication.getName());
    }

    @Operation(
            summary = "Search and filter tickets",
            description = "Returns tickets accessible to the authenticated user. "
                    + "Supports keyword search, status, priority, category, SLA-breach filtering "
                    + "and database-level pagination."
    )
    @GetMapping
    public Page<TicketResponse> getTickets(
            @Parameter(
                    description = "Filter by ticket status",
                    example = "OPEN"
            )
            @RequestParam(required = false)
            TicketStatus status,

            @Parameter(
                    description = "Filter by ticket priority",
                    example = "HIGH"
            )
            @RequestParam(required = false)
            TicketPriority priority,

            @Parameter(
                    description = "Filter by ticket category",
                    example = "PAYMENT"
            )
            @RequestParam(required = false)
            TicketCategory category,

            @Parameter(
                    description = "Filter tickets by SLA breach state",
                    example = "true"
            )
            @RequestParam(required = false)
            Boolean slaBreached,

            @Parameter(
                    description = "Search title and description using a keyword",
                    example = "payment"
            )
            @RequestParam(required = false)
            String keyword,

            @Parameter(
                    description = "Pagination and sorting options"
            )
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC)
            Pageable pageable,

            Authentication authentication) {

        return ticketService.getTickets(
                status,
                priority,
                category,
                slaBreached,
                keyword,
                pageable,
                authentication.getName());
    }

    @Operation(
            summary = "Get a ticket by ID",
            description = "Returns a ticket when the authenticated user is authorized to access it."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Ticket retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "User is not allowed to access this ticket"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Ticket not found"
            )
    })
    @GetMapping("/{id}")
    public TicketResponse getTicket(
            @Parameter(
                    description = "Unique ticket ID",
                    example = "143"
            )
            @PathVariable Long id,
            Authentication authentication) {

        return ticketService.getTicket(
                id,
                authentication.getName());
    }

    @Operation(
            summary = "Update ticket status",
            description = "Changes a ticket's status using the backend state machine. "
                    + "Only valid workflow transitions are accepted."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Ticket status updated successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid status transition or request"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "User does not have permission to update status"
            )
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public TicketResponse updateStatus(
            @Parameter(
                    description = "Unique ticket ID",
                    example = "143"
            )
            @PathVariable Long id,

            @Valid @RequestBody UpdateTicketStatusRequest request,

            Authentication authentication) {

        return ticketService.updateStatus(
                id,
                request,
                authentication.getName());
    }

    @Operation(
            summary = "Assign an agent to a ticket",
            description = "Assigns an agent to a ticket. "
                    + "The backend verifies that the selected user is an agent "
                    + "and belongs to the ticket's support team."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Agent assigned successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid assignment request"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Only admins can assign agents"
            )
    })
    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public TicketResponse assignAgent(
            @Parameter(
                    description = "Unique ticket ID",
                    example = "143"
            )
            @PathVariable Long id,

            @Valid @RequestBody AssignAgentRequest request,

            Authentication authentication) {

        return ticketService.assignAgent(
                id,
                request,
                authentication.getName());
    }

    @Operation(
            summary = "Get ticket history",
            description = "Returns the audit history of changes made to the ticket."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Ticket history retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "User is not allowed to access the ticket history"
            )
    })
    @GetMapping("/{id}/history")
    public List<TicketHistoryResponse> getTicketHistory(
            @Parameter(
                    description = "Unique ticket ID",
                    example = "143"
            )
            @PathVariable Long id) {

        return ticketHistoryService.getTicketHistory(id);
    }

    @Operation(
            summary = "Get admin ticket dashboard summary",
            description = "Returns system-wide ticket statistics including "
                    + "open, in-progress, resolved, breached, unassigned and critical tickets."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Dashboard summary retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Only admins can access the dashboard"
            )
    })
    @GetMapping("/admin/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDashboardResponse getAdminDashboard() {

        return ticketService.getAdminDashboard();
    }
}