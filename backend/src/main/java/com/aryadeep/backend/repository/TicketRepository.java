package com.aryadeep.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aryadeep.backend.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}