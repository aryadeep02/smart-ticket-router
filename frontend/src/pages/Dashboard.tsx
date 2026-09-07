import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import TicketCard from "../components/TicketCard";
import Navbar from "../components/Navbar";
import { useAuth } from "../context/AuthContext";

import {
  getTickets,
  getAdminDashboardSummary,
} from "../services/ticketService";

import type { Ticket } from "../types/ticket";

export default function Dashboard() {
  const navigate = useNavigate();
  const { user } = useAuth();

  const [tickets, setTickets] = useState<Ticket[]>([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [statusFilter, setStatusFilter] = useState("");
  const [priorityFilter, setPriorityFilter] = useState("");
  const [categoryFilter, setCategoryFilter] = useState("");
  const [slaFilter, setSlaFilter] = useState("");

  const [adminSummary, setAdminSummary] = useState({
    totalTickets: 0,
    openTickets: 0,
    inProgressTickets: 0,
    resolvedTickets: 0,
    breachedTickets: 0,
    unassignedTickets: 0,
    criticalTickets: 0,
  });

  useEffect(() => {
    const loadTickets = async () => {
      setLoading(true);
      setError("");

      try {
        const data = await getTickets({
          status: statusFilter || undefined,
          priority: priorityFilter || undefined,
          category: categoryFilter || undefined,
          slaBreached:
            slaFilter === "" ? undefined : slaFilter === "true",
        });

        setTickets(data);
      } catch {
        setError("Failed to load tickets");
      } finally {
        setLoading(false);
      }
    };

    loadTickets();
  }, [statusFilter, priorityFilter, categoryFilter, slaFilter]);

  useEffect(() => {
    if (user?.role !== "ADMIN") {
      return;
    }

    getAdminDashboardSummary()
      .then(setAdminSummary)
      .catch((error) => {
        console.error(
          "Failed to load admin dashboard summary:",
          error
        );
      });
  }, [user?.role]);

  return (
    <div>
      <Navbar />

      <main>
        <div className="dashboard-header">
          <div>
            <h1>
              {user?.role === "CUSTOMER"
                ? "My Tickets"
                : user?.role === "AGENT"
                ? "Agent Dashboard"
                : "Admin Dashboard"}
            </h1>

            <p className="dashboard-subtitle">
              {user?.role === "CUSTOMER"
                ? "Create and track your support requests."
                : user?.role === "AGENT"
                ? "Manage tickets assigned to your support team."
                : "Monitor tickets, users and support operations."}
            </p>
          </div>

          {user?.role === "CUSTOMER" && (
            <button
              className="primary-button"
              onClick={() => navigate("/tickets/create")}
            >
              + Create Ticket
            </button>
          )}
        </div>

        {loading && <p>Loading tickets...</p>}

        {error && <p>{error}</p>}

        {!loading && !error && (
          <>
            {/* Ticket Statistics */}
            <div className="stats-grid">
              <div className="stat-card">
                <h3>Total</h3>
                <p className="stat-value">{tickets.length}</p>
              </div>

              <div className="stat-card">
                <h3>Open</h3>
                <p className="stat-value">
                  {
                    tickets.filter(
                      (ticket) => ticket.status === "OPEN"
                    ).length
                  }
                </p>
              </div>

              <div className="stat-card">
                <h3>In Progress</h3>
                <p className="stat-value">
                  {
                    tickets.filter(
                      (ticket) =>
                        ticket.status === "IN_PROGRESS"
                    ).length
                  }
                </p>
              </div>

              <div className="stat-card">
                <h3>Resolved</h3>
                <p className="stat-value">
                  {
                    tickets.filter(
                      (ticket) => ticket.status === "RESOLVED"
                    ).length
                  }
                </p>
              </div>
            </div>

            {/* Admin Statistics */}
            {user?.role === "ADMIN" && (
              <div className="stats-grid">
                <div className="stat-card">
                  <h3>Admin Overview</h3>
                  <p className="stat-value">
                    {adminSummary.totalTickets}
                  </p>
                </div>

                <div className="stat-card">
                  <h3>SLA Breached</h3>
                  <p className="stat-value">
                    {adminSummary.breachedTickets}
                  </p>
                </div>

                <div className="stat-card">
                  <h3>Unassigned</h3>
                  <p className="stat-value">
                    {adminSummary.unassignedTickets}
                  </p>
                </div>

                <div className="stat-card">
                  <h3>Critical</h3>
                  <p className="stat-value">
                    {adminSummary.criticalTickets}
                  </p>
                </div>
              </div>
            )}

            {/* Filters */}
            <div className="filters">
              <select
                className="filter-select"
                value={statusFilter}
                onChange={(event) =>
                  setStatusFilter(event.target.value)
                }
              >
                <option value="">All Statuses</option>
                <option value="OPEN">OPEN</option>
                <option value="IN_PROGRESS">IN_PROGRESS</option>
                <option value="WAITING_FOR_CUSTOMER">
                  WAITING_FOR_CUSTOMER
                </option>
                <option value="RESOLVED">RESOLVED</option>
                <option value="CLOSED">CLOSED</option>
              </select>

              <select
                className="filter-select"
                value={priorityFilter}
                onChange={(event) =>
                  setPriorityFilter(event.target.value)
                }
              >
                <option value="">All Priorities</option>
                <option value="LOW">LOW</option>
                <option value="MEDIUM">MEDIUM</option>
                <option value="HIGH">HIGH</option>
                <option value="CRITICAL">CRITICAL</option>
              </select>

              <select
                className="filter-select"
                value={categoryFilter}
                onChange={(event) =>
                  setCategoryFilter(event.target.value)
                }
              >
                <option value="">All Categories</option>
                <option value="ACCOUNT">ACCOUNT</option>
                <option value="PAYMENT">PAYMENT</option>
                <option value="TECHNICAL">TECHNICAL</option>
                <option value="DELIVERY">DELIVERY</option>
                <option value="REFUND">REFUND</option>
                <option value="OTHER">OTHER</option>
              </select>

              <select
                className="filter-select"
                value={slaFilter}
                onChange={(event) =>
                  setSlaFilter(event.target.value)
                }
              >
                <option value="">All SLA</option>
                <option value="true">Breached</option>
                <option value="false">Within SLA</option>
              </select>
            </div>

            {/* Tickets */}
            <div className="tickets-section">
              {tickets.length === 0 && (
                <p>No tickets found.</p>
              )}

              {tickets.map((ticket) => (
                <TicketCard
                  key={ticket.id}
                  ticket={ticket}
                />
              ))}
            </div>
          </>
        )}
      </main>
    </div>
  );
}