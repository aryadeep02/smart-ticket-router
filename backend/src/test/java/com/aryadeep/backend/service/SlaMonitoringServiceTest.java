package com.aryadeep.backend.service;

import com.aryadeep.backend.entity.Ticket;
import com.aryadeep.backend.entity.TicketStatus;
import com.aryadeep.backend.repository.TicketRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SlaMonitoringServiceTest {

    private static final List<TicketStatus> EXCLUDED_STATUSES =
            List.of(
                    TicketStatus.RESOLVED,
                    TicketStatus.CLOSED
            );

    @Test
    void shouldMarkExpiredOpenTicketAsBreached() {

        TicketRepository ticketRepository =
                mock(TicketRepository.class);

        Ticket ticket = Ticket.builder()
                .id(100L)
                .status(TicketStatus.OPEN)
                .slaDeadline(LocalDateTime.now().minusHours(1))
                .slaBreached(false)
                .build();

        doReturn(List.of(ticket))
                .when(ticketRepository)
                .findBySlaBreachedFalseAndStatusNotInAndSlaDeadlineBefore(
                        eq(EXCLUDED_STATUSES),
                        any(LocalDateTime.class));

        SlaMonitoringService service =
                new SlaMonitoringService(ticketRepository);

        service.processSlaBreaches();

        assertTrue(ticket.getSlaBreached());

        verify(ticketRepository).save(ticket);
    }

    @Test
    void shouldNotMarkResolvedTicketAsBreached() {

        TicketRepository ticketRepository =
                mock(TicketRepository.class);

        Ticket ticket = Ticket.builder()
                .id(101L)
                .status(TicketStatus.RESOLVED)
                .slaDeadline(LocalDateTime.now().minusHours(1))
                .slaBreached(false)
                .build();

        doReturn(List.of())
                .when(ticketRepository)
                .findBySlaBreachedFalseAndStatusNotInAndSlaDeadlineBefore(
                        eq(EXCLUDED_STATUSES),
                        any(LocalDateTime.class));

        SlaMonitoringService service =
                new SlaMonitoringService(ticketRepository);

        service.processSlaBreaches();

        assertFalse(ticket.getSlaBreached());

        verify(ticketRepository, never()).save(ticket);
    }

    @Test
    void shouldNotMarkFutureTicketAsBreached() {

        TicketRepository ticketRepository =
                mock(TicketRepository.class);

        Ticket ticket = Ticket.builder()
                .id(102L)
                .status(TicketStatus.OPEN)
                .slaDeadline(LocalDateTime.now().plusHours(1))
                .slaBreached(false)
                .build();

        doReturn(List.of())
                .when(ticketRepository)
                .findBySlaBreachedFalseAndStatusNotInAndSlaDeadlineBefore(
                        eq(EXCLUDED_STATUSES),
                        any(LocalDateTime.class));

        SlaMonitoringService service =
                new SlaMonitoringService(ticketRepository);

        service.processSlaBreaches();

        assertFalse(ticket.getSlaBreached());

        verify(ticketRepository, never()).save(ticket);
    }
}