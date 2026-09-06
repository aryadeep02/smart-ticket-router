package com.aryadeep.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aryadeep.backend.dto.CreateTicketRequest;
import com.aryadeep.backend.dto.TicketHistoryResponse;
import com.aryadeep.backend.dto.TicketResponse;
import com.aryadeep.backend.dto.UpdateTicketStatusRequest;
import com.aryadeep.backend.service.TicketHistoryService;
import com.aryadeep.backend.service.TicketService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final TicketHistoryService ticketHistoryService;

    public TicketController(
            TicketService ticketService,
            TicketHistoryService ticketHistoryService) {

        this.ticketService = ticketService;
        this.ticketHistoryService = ticketHistoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            Authentication authentication) {

        return ticketService.createTicket(
                request,
                authentication.getName());
    }

    @PatchMapping("/{id}/status")
    public TicketResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketStatusRequest request,
            Authentication authentication) {

        return ticketService.updateStatus(
                id,
                request,
                authentication.getName());
    }

    @GetMapping("/{id}/history")
    public List<TicketHistoryResponse> getTicketHistory(
            @PathVariable Long id) {

        return ticketHistoryService.getTicketHistory(id);
    }
}