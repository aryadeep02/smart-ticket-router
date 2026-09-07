import { useNavigate } from "react-router-dom";

import { useAuth } from "../context/AuthContext";

import type { Ticket } from "../types/ticket";

interface TicketCardProps {
  ticket: Ticket;
}

export default function TicketCard({
  ticket,
}: TicketCardProps) {
  const navigate = useNavigate();
  const { user } = useAuth();

  return (
    <div
      className="ticket-card"
      onClick={() => navigate(`/tickets/${ticket.id}`)}
      role="button"
      tabIndex={0}
      onKeyDown={(event) => {
        if (event.key === "Enter") {
          navigate(`/tickets/${ticket.id}`);
        }
      }}
    >
      <div className="ticket-card-header">
        <h3>{ticket.title}</h3>

        <span
          className={`priority-badge priority-${ticket.priority.toLowerCase()}`}
        >
          {ticket.priority}
        </span>
      </div>

      <p className="ticket-description">
        {ticket.description}
      </p>

      <div className="ticket-meta">
        <span>{ticket.category}</span>

        <span>•</span>

        <span>{ticket.status}</span>

        {ticket.slaBreached && (
          <>
            <span>•</span>

            <span className="sla-breached">
              SLA BREACHED
            </span>
          </>
        )}

        {user?.role === "AGENT" && (
          <>
            <span>•</span>

            <span>
              {ticket.assignedAgentId === user.userId
                ? "ASSIGNED TO YOU"
                : "TEAM TICKET"}
            </span>
          </>
        )}
      </div>
    </div>
  );
}