package com.aryadeep.backend.service;

import com.aryadeep.backend.entity.Role;
import com.aryadeep.backend.entity.SupportTeam;
import com.aryadeep.backend.entity.Ticket;
import com.aryadeep.backend.entity.User;
import org.springframework.stereotype.Service;

@Service
public class TicketAccessService {

    public boolean canAccess(
            Ticket ticket,
            User user) {

        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        if (user.getRole() == Role.CUSTOMER) {
            return ticket.getCustomer().getId()
                    .equals(user.getId());
        }

        if (user.getRole() == Role.AGENT) {

            if (ticket.getAssignedAgent() != null
                    && ticket.getAssignedAgent().getId()
                    .equals(user.getId())) {

                return true;
            }

            SupportTeam agentTeam = user.getSupportTeam();
            SupportTeam ticketTeam = ticket.getSupportTeam();

            return agentTeam != null
                    && ticketTeam != null
                    && agentTeam.getId()
                    .equals(ticketTeam.getId());
        }

        return false;
    }
}