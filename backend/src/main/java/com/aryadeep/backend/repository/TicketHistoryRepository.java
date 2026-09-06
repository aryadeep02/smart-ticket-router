package com.aryadeep.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aryadeep.backend.entity.TicketHistory;

public interface TicketHistoryRepository
        extends JpaRepository<TicketHistory, Long> {

    List<TicketHistory> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}