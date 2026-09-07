package com.aryadeep.backend.service;

import com.aryadeep.backend.entity.Ticket;
import com.aryadeep.backend.entity.TicketStatus;
import com.aryadeep.backend.repository.TicketRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SlaMonitoringService {

    private final TicketRepository ticketRepository;

    public SlaMonitoringService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Scheduled(fixedRate = 300000)
    public void detectSlaBreaches() {
        processSlaBreaches();
    }
    public void processSlaBreaches() {

        LocalDateTime now = LocalDateTime.now();
    
        List<Ticket> expiredTickets =
                ticketRepository
                        .findBySlaBreachedFalseAndStatusNotInAndSlaDeadlineBefore(
                                List.of(
                                        TicketStatus.RESOLVED,
                                        TicketStatus.CLOSED
                                ),
                                now);
    
        for (Ticket ticket : expiredTickets) {
            ticket.setSlaBreached(true);
            ticketRepository.save(ticket);
        }
    }   
}