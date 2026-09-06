package com.aryadeep.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.aryadeep.backend.dto.TicketHistoryResponse;
import com.aryadeep.backend.entity.Ticket;
import com.aryadeep.backend.entity.TicketHistory;
import com.aryadeep.backend.entity.User;
import com.aryadeep.backend.repository.TicketHistoryRepository;

@Service
public class TicketHistoryService {

    private final TicketHistoryRepository ticketHistoryRepository;

    public TicketHistoryService(
            TicketHistoryRepository ticketHistoryRepository) {
        this.ticketHistoryRepository = ticketHistoryRepository;
    }

    public void record(
            Ticket ticket,
            User changedBy,
            String action,
            String oldValue,
            String newValue) {

        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .changedBy(changedBy)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();

        ticketHistoryRepository.save(history);
    }

    public List<TicketHistoryResponse> getTicketHistory(Long ticketId) {

        return ticketHistoryRepository
                .findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TicketHistoryResponse toResponse(
            TicketHistory history) {

        return new TicketHistoryResponse(
                history.getId(),
                history.getTicket().getId(),
                history.getChangedBy() != null
                        ? history.getChangedBy().getId()
                        : null,
                history.getAction(),
                history.getOldValue(),
                history.getNewValue(),
                history.getCreatedAt()
        );
    }
}