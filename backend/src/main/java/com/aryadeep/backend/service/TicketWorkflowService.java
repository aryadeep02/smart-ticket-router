package com.aryadeep.backend.service;

import org.springframework.stereotype.Service;

import com.aryadeep.backend.entity.TicketStatus;

@Service
public class TicketWorkflowService {

    public boolean isValidTransition(
            TicketStatus currentStatus,
            TicketStatus newStatus) {

        return switch (currentStatus) {

            case OPEN ->
                    newStatus == TicketStatus.IN_PROGRESS
                            || newStatus == TicketStatus.RESOLVED;

            case IN_PROGRESS ->
                    newStatus == TicketStatus.WAITING_FOR_CUSTOMER
                            || newStatus == TicketStatus.RESOLVED;

            case WAITING_FOR_CUSTOMER ->
                    newStatus == TicketStatus.IN_PROGRESS
                            || newStatus == TicketStatus.RESOLVED;

            case RESOLVED ->
                    newStatus == TicketStatus.CLOSED;

            case CLOSED ->
                    false;
        };
    }
}