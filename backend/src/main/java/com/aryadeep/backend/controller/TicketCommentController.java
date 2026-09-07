package com.aryadeep.backend.controller;

import com.aryadeep.backend.dto.CreateCommentRequest;
import com.aryadeep.backend.dto.TicketCommentResponse;
import com.aryadeep.backend.service.TicketCommentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/comments")
public class TicketCommentController {

    private final TicketCommentService ticketCommentService;

    public TicketCommentController(
            TicketCommentService ticketCommentService) {

        this.ticketCommentService = ticketCommentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketCommentResponse addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {

        return ticketCommentService.addComment(
                ticketId,
                request,
                authentication.getName());
    }

    @GetMapping
    public List<TicketCommentResponse> getComments(
            @PathVariable Long ticketId,
            Authentication authentication) {

        return ticketCommentService.getComments(
                ticketId,
                authentication.getName());
    }
}