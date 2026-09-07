package com.aryadeep.backend.service;

import com.aryadeep.backend.dto.CreateCommentRequest;
import com.aryadeep.backend.dto.TicketCommentResponse;
import com.aryadeep.backend.entity.Ticket;
import com.aryadeep.backend.entity.TicketComment;
import com.aryadeep.backend.entity.User;
import com.aryadeep.backend.repository.TicketCommentRepository;
import com.aryadeep.backend.repository.TicketRepository;
import com.aryadeep.backend.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketCommentService {

    private final TicketCommentRepository ticketCommentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketAccessService ticketAccessService;

    public TicketCommentService(
            TicketCommentRepository ticketCommentRepository,
            TicketRepository ticketRepository,
            UserRepository userRepository,
            TicketAccessService ticketAccessService) {

        this.ticketCommentRepository = ticketCommentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketAccessService = ticketAccessService;
    }

    public TicketCommentResponse addComment(
            Long ticketId,
            CreateCommentRequest request,
            String userEmail) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new RuntimeException("Ticket not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!ticketAccessService.canAccess(ticket, user)) {
            throw new AccessDeniedException(
                    "You do not have permission to comment on this ticket");
        }

        TicketComment comment = TicketComment.builder()
                .ticket(ticket)
                .author(user)
                .content(request.content())
                .build();

        TicketComment savedComment =
                ticketCommentRepository.save(comment);

        return toResponse(savedComment);
    }

    public List<TicketCommentResponse> getComments(
            Long ticketId,
            String userEmail) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new RuntimeException("Ticket not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!ticketAccessService.canAccess(ticket, user)) {
            throw new AccessDeniedException(
                    "You do not have permission to view comments on this ticket");
        }

        return ticketCommentRepository
                .findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TicketCommentResponse toResponse(TicketComment comment) {

        return new TicketCommentResponse(
                comment.getId(),
                comment.getTicket().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getName(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}