package com.aryadeep.backend.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.aryadeep.backend.dto.AIClassificationResponse;
import com.aryadeep.backend.dto.CreateTicketRequest;
import com.aryadeep.backend.dto.TicketResponse;
import com.aryadeep.backend.dto.UpdateTicketStatusRequest;
import com.aryadeep.backend.entity.SupportTeam;
import com.aryadeep.backend.entity.Ticket;
import com.aryadeep.backend.entity.TicketCategory;
import com.aryadeep.backend.entity.TicketPriority;
import com.aryadeep.backend.entity.TicketStatus;
import com.aryadeep.backend.entity.User;
import com.aryadeep.backend.repository.SupportTeamRepository;
import com.aryadeep.backend.repository.TicketRepository;
import com.aryadeep.backend.repository.UserRepository;

@Service
public class TicketService {

        private final TicketHistoryService ticketHistoryService;

        private final TicketRepository ticketRepository;
        private final UserRepository userRepository;
        private final SupportTeamRepository supportTeamRepository;
        private final AIClassificationService aiClassificationService;
        private final TicketWorkflowService ticketWorkflowService;

        public TicketService(
                        TicketRepository ticketRepository,
                        UserRepository userRepository,
                        SupportTeamRepository supportTeamRepository,
                        AIClassificationService aiClassificationService,
                        TicketWorkflowService ticketWorkflowService,
                        TicketHistoryService ticketHistoryService) {

                this.ticketRepository = ticketRepository;
                this.userRepository = userRepository;
                this.supportTeamRepository = supportTeamRepository;
                this.aiClassificationService = aiClassificationService;
                this.ticketWorkflowService = ticketWorkflowService;
                this.ticketHistoryService = ticketHistoryService;
        }

        public TicketResponse createTicket(
                        CreateTicketRequest request,
                        String customerEmail) {

                User customer = userRepository.findByEmail(customerEmail)
                                .orElseThrow(() -> new RuntimeException("Customer not found"));

                AIClassificationResponse aiResponse;

                try {
                        aiResponse = aiClassificationService.classify(
                                        request.title(),
                                        request.description());
                } catch (Exception exception) {

                        aiResponse = null;
                }

                TicketCategory category;
                TicketPriority priority;
                Double aiConfidence;
                String aiClassificationStatus;

                if (aiResponse == null || aiResponse.confidence() < 0.70) {

                        category = TicketCategory.OTHER;
                        priority = TicketPriority.MEDIUM;

                        aiConfidence = aiResponse != null
                                        ? aiResponse.confidence()
                                        : null;

                        aiClassificationStatus = "FALLBACK";

                } else {

                        category = TicketCategory.valueOf(
                                        aiResponse.category());

                        priority = TicketPriority.valueOf(
                                        aiResponse.priority());

                        aiConfidence = aiResponse.confidence();
                        aiClassificationStatus = "COMPLETED";
                }

                LocalDateTime slaDeadline = calculateSlaDeadline(priority);

                SupportTeam supportTeam = routeToSupportTeam(category);

                Ticket ticket = Ticket.builder()
                                .title(request.title())
                                .description(request.description())
                                .category(category)
                                .priority(priority)
                                .status(TicketStatus.OPEN)
                                .customer(customer)
                                .supportTeam(supportTeam)
                                .aiConfidence(aiConfidence)
                                .aiClassificationStatus(aiClassificationStatus)
                                .slaDeadline(slaDeadline)
                                .build();

                Ticket savedTicket = ticketRepository.save(ticket);

                ticketHistoryService.record(
                                savedTicket,
                                customer,
                                "TICKET_CREATED",
                                null,
                                TicketStatus.OPEN.name());

                return toResponse(savedTicket);
        }

        public TicketResponse updateStatus(
                Long ticketId,
                UpdateTicketStatusRequest request,
                String userEmail) {
        
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() ->
                            new RuntimeException("Ticket not found"));
        
            User changedBy = userRepository.findByEmail(userEmail)
                    .orElseThrow(() ->
                            new RuntimeException("User not found"));
        
            TicketStatus currentStatus = ticket.getStatus();
            TicketStatus newStatus = request.status();
        
            if (!ticketWorkflowService.isValidTransition(
                    currentStatus,
                    newStatus)) {
        
                throw new IllegalStateException(
                        "Invalid ticket status transition: "
                                + currentStatus
                                + " -> "
                                + newStatus
                );
            }
        
            ticket.setStatus(newStatus);
        
            if (newStatus == TicketStatus.RESOLVED) {
                ticket.setResolvedAt(LocalDateTime.now());
            }
        
            Ticket updatedTicket =
                    ticketRepository.save(ticket);
        
            ticketHistoryService.record(
                    updatedTicket,
                    changedBy,
                    "STATUS_CHANGED",
                    currentStatus.name(),
                    newStatus.name()
            );
        
            return toResponse(updatedTicket);
        }

        private LocalDateTime calculateSlaDeadline(
                        TicketPriority priority) {

                int slaHours = switch (priority) {
                        case CRITICAL -> 1;
                        case HIGH -> 4;
                        case MEDIUM -> 12;
                        case LOW -> 24;
                };

                return LocalDateTime.now().plusHours(slaHours);
        }

        private SupportTeam routeToSupportTeam(
                        TicketCategory category) {

                String teamName = switch (category) {

                        case ACCOUNT ->
                                "ACCOUNT_SUPPORT";

                        case PAYMENT, REFUND ->
                                "PAYMENT_SUPPORT";

                        case TECHNICAL ->
                                "TECHNICAL_SUPPORT";

                        case DELIVERY ->
                                "DELIVERY_SUPPORT";

                        case OTHER ->
                                "GENERAL_SUPPORT";
                };

                return supportTeamRepository.findByName(teamName)
                                .orElseThrow(() -> new RuntimeException(
                                                "Support team not found: " + teamName));
        }

        private TicketResponse toResponse(Ticket ticket) {

                return new TicketResponse(
                                ticket.getId(),
                                ticket.getTitle(),
                                ticket.getDescription(),
                                ticket.getCategory(),
                                ticket.getPriority(),
                                ticket.getStatus(),
                                ticket.getCustomer().getId(),
                                ticket.getAssignedAgent() != null
                                                ? ticket.getAssignedAgent().getId()
                                                : null,
                                ticket.getSupportTeam() != null
                                                ? ticket.getSupportTeam().getId()
                                                : null,
                                ticket.getAiConfidence(),
                                ticket.getAiClassificationStatus(),
                                ticket.getSlaDeadline(),
                                ticket.getSlaBreached(),
                                ticket.getResolvedAt(),
                                ticket.getCreatedAt(),
                                ticket.getUpdatedAt());
        }
}