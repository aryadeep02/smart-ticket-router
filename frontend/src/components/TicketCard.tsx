import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import type { Ticket } from "../types/ticket";

interface TicketCardProps {
  ticket: Ticket;
}

export default function TicketCard({ ticket }: TicketCardProps) {
  const navigate = useNavigate();
  const { user } = useAuth();

  const priorityClass = ticket.priority.toLowerCase();
  const statusClass = ticket.status.toLowerCase().replaceAll("_", "-");

  return (
    <article
      className="ticket-card"
      onClick={() => navigate(`/tickets/${ticket.id}`)}
      role="button"
      tabIndex={0}
      onKeyDown={(event) => {
        if (event.key === "Enter" || event.key === " ") {
          event.preventDefault();
          navigate(`/tickets/${ticket.id}`);
        }
      }}
    >
      <div className="ticket-card-top">
        <div className="ticket-id">
          TICKET #{ticket.id}
        </div>

        <span className={`priority-badge priority-${priorityClass}`}>
          {ticket.priority}
        </span>
      </div>

      <div className="ticket-card-header">
        <h3>{ticket.title}</h3>

        <span className={`ticket-status status-${statusClass}`}>
          {ticket.status.replaceAll("_", " ")}
        </span>
      </div>

      <p className="ticket-description">
        {ticket.description}
      </p>

      <div className="ticket-card-footer">
        <div className="ticket-meta">
          <span>{ticket.category}</span>
          <span className="meta-dot">•</span>
          <span>
            {ticket.supportTeamId
              ? `Team #${ticket.supportTeamId}`
              : "Unassigned"}
          </span>
        </div>

        {ticket.slaBreached && (
          <span className="sla-breached">
            SLA BREACHED
          </span>
        )}

        {user?.role === "AGENT" && (
          <span
            className={
              ticket.assignedAgentId === user.userId
                ? "assignment-badge assigned"
                : "assignment-badge"
            }
          >
            {ticket.assignedAgentId === user.userId
              ? "Assigned to you"
              : "Team ticket"}
          </span>
        )}
      </div>
    </article>
  );
}