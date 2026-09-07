package com.aryadeep.backend.service;

import com.aryadeep.backend.entity.Ticket;
import com.aryadeep.backend.entity.TicketCategory;
import com.aryadeep.backend.entity.TicketPriority;
import com.aryadeep.backend.entity.TicketStatus;
import com.aryadeep.backend.entity.User;
import com.aryadeep.backend.repository.TicketRepository;
import com.aryadeep.backend.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SlaMonitoringServiceIntegrationTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SlaMonitoringService slaMonitoringService;

    @Test
    void shouldMarkExpiredTicketAsBreached() {

        User customer = userRepository.findByEmail("test@example.com")
                .orElseThrow();

        Ticket ticket = Ticket.builder()
                .title("SLA integration test")
                .description("Test expired SLA ticket")
                .category(TicketCategory.PAYMENT)
                .priority(TicketPriority.HIGH)
                .status(TicketStatus.OPEN)
                .customer(customer)
                .slaDeadline(LocalDateTime.now().minusHours(1))
                .slaBreached(false)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);

        slaMonitoringService.processSlaBreaches();

        Ticket updatedTicket = ticketRepository
                .findById(savedTicket.getId())
                .orElseThrow();

        assertTrue(updatedTicket.getSlaBreached());

        ticketRepository.delete(updatedTicket);
    }
}