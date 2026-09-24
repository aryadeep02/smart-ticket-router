import { useEffect, useState } from "react";
import Navbar from "../components/Navbar";
import TicketCard from "../components/TicketCard";
import { useAuth } from "../context/AuthContext";
import {
  getAdminDashboardSummary,
  getTickets,
} from "../services/ticketService";
import type { Ticket } from "../types/ticket";

interface AdminSummary {
  totalTickets: number;
  openTickets: number;
  inProgressTickets: number;
  resolvedTickets: number;
  breachedTickets: number;
  unassignedTickets: number;
  criticalTickets: number;
}

export default function Dashboard() {
  const { user } = useAuth();

  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [status, setStatus] = useState("");
  const [priority, setPriority] = useState("");
  const [category, setCategory] = useState("");
  const [slaBreached, setSlaBreached] = useState("");

  const [adminSummary, setAdminSummary] = useState<AdminSummary | null>(null);

  const loadTickets = async () => {
    try {
      setLoading(true);
      setError("");

      const data = await getTickets({
        status: status || undefined,
        priority: priority || undefined,
        category: category || undefined,
        slaBreached: slaBreached === "" ? undefined : slaBreached === "true",
      });

      setTickets(data.content);
    } catch {
      setError("Unable to load tickets. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const loadAdminSummary = async () => {
    if (user?.role !== "ADMIN") return;

    try {
      const summary = await getAdminDashboardSummary();
      setAdminSummary(summary);
    } catch {
      // Dashboard tickets can still work if admin analytics fail.
    }
  };

  useEffect(() => {
    loadTickets();
  }, [status, priority, category, slaBreached]);

  useEffect(() => {
    loadAdminSummary();
  }, [user?.role]);

  const clearFilters = () => {
    setStatus("");
    setPriority("");
    setCategory("");
    setSlaBreached("");
  };

  const hasFilters = status || priority || category || slaBreached;

  const stats = [
    {
      label: "Total tickets",
      value: tickets.length,
      className: "stat-neutral",
    },
    {
      label: "Open",
      value: tickets.filter((t) => t.status === "OPEN").length,
      className: "stat-blue",
    },
    {
      label: "In progress",
      value: tickets.filter((t) => t.status === "IN_PROGRESS").length,
      className: "stat-purple",
    },
    {
      label: "Resolved",
      value: tickets.filter(
        (t) => t.status === "RESOLVED" || t.status === "CLOSED"
      ).length,
      className: "stat-green",
    },
  ];

  return (
    <>
      <Navbar />

      <main className="dashboard-page">
        <section className="dashboard-hero">
          <div>
            <div className="eyebrow">
              {user?.role === "ADMIN" ? "Administration" : "Workspace"}
            </div>

            <h1>
              Welcome back, <span>{user?.name?.split(" ")[0] || "there"}</span>
            </h1>

            <p>Track, manage and resolve support tickets from one place.</p>
          </div>

          <div className="dashboard-role">
            <span className="role-dot" />
            {user?.role || "USER"}
          </div>
        </section>

        <section className="stats-grid">
          {stats.map((stat) => (
            <div key={stat.label} className={`stat-card ${stat.className}`}>
              <div className="stat-label">{stat.label}</div>
              <div className="stat-value">{loading ? "—" : stat.value}</div>
            </div>
          ))}
        </section>

        {user?.role === "ADMIN" && adminSummary && (
          <section className="admin-overview">
            <div className="section-heading">
              <div>
                <span className="eyebrow">System overview</span>
                <h2>Operations</h2>
              </div>

              <span className="live-indicator">
                <span />
                Live
              </span>
            </div>

            <div className="admin-stat-grid">
              <div>
                <span>Total</span>
                <strong>{adminSummary.totalTickets}</strong>
              </div>

              <div>
                <span>Unassigned</span>
                <strong>{adminSummary.unassignedTickets}</strong>
              </div>

              <div>
                <span>Critical</span>
                <strong>{adminSummary.criticalTickets}</strong>
              </div>

              <div>
                <span>SLA breached</span>
                <strong>{adminSummary.breachedTickets}</strong>
              </div>
            </div>
          </section>
        )}

        <section className="tickets-section">
          <div className="section-heading">
            <div>
              <span className="eyebrow">Support queue</span>
              <h2>Your tickets</h2>
            </div>

            <div className="dashboard-ticket-actions">
              <span className="ticket-count">
                {tickets.length} result
                {tickets.length === 1 ? "" : "s"}
              </span>

              <button
                type="button"
                className="dashboard-view-all"
                onClick={() => {
                  window.location.href = "/tickets";
                }}
              >
                View all tickets →
              </button>
            </div>
          </div>

          <div className="ticket-filters">
            <select value={status} onChange={(e) => setStatus(e.target.value)}>
              <option value="">All statuses</option>
              <option value="OPEN">Open</option>
              <option value="IN_PROGRESS">In progress</option>
              <option value="WAITING_FOR_CUSTOMER">Waiting for customer</option>
              <option value="RESOLVED">Resolved</option>
              <option value="CLOSED">Closed</option>
            </select>

            <select
              value={priority}
              onChange={(e) => setPriority(e.target.value)}
            >
              <option value="">All priorities</option>
              <option value="CRITICAL">Critical</option>
              <option value="HIGH">High</option>
              <option value="MEDIUM">Medium</option>
              <option value="LOW">Low</option>
            </select>

            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
            >
              <option value="">All categories</option>
              <option value="ACCOUNT">Account</option>
              <option value="PAYMENT">Payment</option>
              <option value="TECHNICAL">Technical</option>
              <option value="DELIVERY">Delivery</option>
              <option value="REFUND">Refund</option>
              <option value="OTHER">Other</option>
            </select>

            <select
              value={slaBreached}
              onChange={(e) => setSlaBreached(e.target.value)}
            >
              <option value="">SLA: All</option>
              <option value="false">SLA healthy</option>
              <option value="true">SLA breached</option>
            </select>

            {hasFilters && (
              <button
                type="button"
                className="clear-filters"
                onClick={clearFilters}
              >
                Clear
              </button>
            )}
          </div>

          {error && (
            <div className="dashboard-error">
              <strong>Something went wrong</strong>
              <span>{error}</span>
              <button onClick={loadTickets}>Retry</button>
            </div>
          )}

          {loading ? (
            <div className="ticket-grid">
              {[1, 2, 3].map((item) => (
                <div className="ticket-skeleton" key={item}>
                  <div />
                  <div />
                  <div />
                </div>
              ))}
            </div>
          ) : tickets.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">✓</div>
              <h3>No tickets found</h3>
              <p>
                {hasFilters
                  ? "Try changing your filters."
                  : "You're all caught up."}
              </p>

              {hasFilters && (
                <button className="secondary-button" onClick={clearFilters}>
                  Clear filters
                </button>
              )}
            </div>
          ) : (
            <div className="ticket-grid">
              {tickets.map((ticket) => (
                <TicketCard key={ticket.id} ticket={ticket} />
              ))}
            </div>
          )}
        </section>
      </main>
    </>
  );
}
