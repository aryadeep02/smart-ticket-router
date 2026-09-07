package com.aryadeep.backend.repository;

import com.aryadeep.backend.entity.Ticket;
import com.aryadeep.backend.entity.TicketPriority;
import com.aryadeep.backend.entity.TicketStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository
        extends JpaRepository<Ticket, Long>,
                JpaSpecificationExecutor<Ticket> {

    List<Ticket> findBySlaBreachedFalseAndStatusNotInAndSlaDeadlineBefore(
            List<TicketStatus> excludedStatuses,
            LocalDateTime deadline);

    long countByStatus(TicketStatus status);

    long countBySlaBreachedTrue();

    long countByAssignedAgentIsNull();

    long countByPriority(TicketPriority priority);
}