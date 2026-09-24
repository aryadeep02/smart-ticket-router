import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import Navbar from "../components/Navbar";
import { getTickets } from "../services/ticketService";

import type {
  Ticket,
  TicketCategory,
  TicketPriority,
  TicketStatus,
} from "../types/ticket";

const statusOptions: TicketStatus[] = [
  "OPEN",
  "IN_PROGRESS",
  "WAITING_FOR_CUSTOMER",
  "RESOLVED",
  "CLOSED",
];

const priorityOptions: TicketPriority[] = [
  "LOW",
  "MEDIUM",
  "HIGH",
  "CRITICAL",
];

const categoryOptions: TicketCategory[] = [
  "ACCOUNT",
  "PAYMENT",
  "TECHNICAL",
  "DELIVERY",
  "REFUND",
  "OTHER",
];

const PAGE_SIZE = 10;

function formatStatus(status: TicketStatus) {
  return status.replaceAll("_", " ");
}

function formatDate(date: string) {
  return new Date(date).toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

function formatDateTime(date: string) {
  return new Date(date).toLocaleString("en-IN", {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function getSlaLabel(ticket: Ticket) {
  if (ticket.slaBreached) {
    return "SLA breached";
  }

  const deadline = new Date(
    ticket.slaDeadline
  ).getTime();

  const difference = deadline - Date.now();

  if (difference <= 0) {
    return "SLA breached";
  }

  const hours = Math.floor(
    difference / (1000 * 60 * 60)
  );

  const minutes = Math.floor(
    (difference % (1000 * 60 * 60)) /
      (1000 * 60)
  );

  if (hours >= 24) {
    const days = Math.floor(hours / 24);
    return `${days}d remaining`;
  }

  if (hours > 0) {
    return `${hours}h ${minutes}m remaining`;
  }

  return `${minutes}m remaining`;
}

export default function TicketList() {
  const navigate = useNavigate();

  const [tickets, setTickets] = useState<Ticket[]>(
    []
  );

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [search, setSearch] = useState("");

  const [status, setStatus] = useState("");
  const [priority, setPriority] = useState("");
  const [category, setCategory] = useState("");
  const [slaBreached, setSlaBreached] =
    useState("");

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] =
    useState(0);
  const [totalElements, setTotalElements] =
    useState(0);

  /* =========================================================
     LOAD TICKETS
  ========================================================= */

  const loadTickets = async (
    requestedPage = page
  ) => {
    try {
      setLoading(true);
      setError("");

      const data = await getTickets(
        {
          status: status || undefined,
          priority: priority || undefined,
          category: category || undefined,
          slaBreached:
            slaBreached === ""
              ? undefined
              : slaBreached === "true",
        },
        requestedPage,
        PAGE_SIZE
      );

      setTickets(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
      setPage(data.number);
    } catch {
      setError(
        "Unable to load tickets. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  /* =========================================================
     FILTER CHANGE
  ========================================================= */

  useEffect(() => {
    setPage(0);

    loadTickets(0);
  }, [
    status,
    priority,
    category,
    slaBreached,
  ]);

  /* =========================================================
     SEARCH
  ========================================================= */

  const filteredTickets = useMemo(() => {
    const query = search
      .trim()
      .toLowerCase();

    if (!query) {
      return tickets;
    }

    return tickets.filter((ticket) => {
      return (
        ticket.title
          .toLowerCase()
          .includes(query) ||
        ticket.description
          .toLowerCase()
          .includes(query) ||
        `#${ticket.id}`.includes(query)
      );
    });
  }, [tickets, search]);

  /* =========================================================
     CLEAR FILTERS
  ========================================================= */

  const clearFilters = () => {
    setSearch("");
    setStatus("");
    setPriority("");
    setCategory("");
    setSlaBreached("");
    setPage(0);
  };

  const hasFilters =
    search ||
    status ||
    priority ||
    category ||
    slaBreached;

  /* =========================================================
     PAGINATION
  ========================================================= */

  const handlePageChange = (
    nextPage: number
  ) => {
    if (
      nextPage < 0 ||
      nextPage >= totalPages ||
      nextPage === page
    ) {
      return;
    }

    setPage(nextPage);
    loadTickets(nextPage);

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  };

  /* =========================================================
     PAGE NUMBERS
  ========================================================= */

  const pageNumbers = Array.from(
    { length: totalPages },
    (_, index) => index
  ).filter((pageNumber) => {
    return (
      pageNumber >= Math.max(0, page - 2) &&
      pageNumber <=
        Math.min(totalPages - 1, page + 2)
    );
  });

  /* =========================================================
     RENDER
  ========================================================= */

  return (
    <div>
      <Navbar />

      <main className="page-container ticket-list-page">
        {/* BACK */}

        <button
          className="secondary-button back-button"
          onClick={() =>
            navigate("/dashboard")
          }
        >
          ← Back to Dashboard
        </button>

        {/* HEADER */}

        <section className="ticket-list-hero">
          <div>
            <span className="ticket-list-eyebrow">
              SUPPORT WORKSPACE
            </span>

            <h1>Tickets</h1>

            <p>
              Monitor, filter and manage customer
              support requests across the platform.
            </p>
          </div>

          <button
            className="primary-button"
            onClick={() =>
              navigate("/tickets/create")
            }
          >
            + New Ticket
          </button>
        </section>

        {/* FILTER TOOLBAR */}

        <section className="ticket-list-toolbar">
          <div className="ticket-search">
            <span>⌕</span>

            <input
              type="text"
              value={search}
              onChange={(event) =>
                setSearch(event.target.value)
              }
              placeholder="Search by title, description or ticket ID..."
            />
          </div>

          <select
            value={status}
            onChange={(event) =>
              setStatus(event.target.value)
            }
          >
            <option value="">
              All statuses
            </option>

            {statusOptions.map((option) => (
              <option
                key={option}
                value={option}
              >
                {formatStatus(option)}
              </option>
            ))}
          </select>

          <select
            value={priority}
            onChange={(event) =>
              setPriority(event.target.value)
            }
          >
            <option value="">
              All priorities
            </option>

            {priorityOptions.map((option) => (
              <option
                key={option}
                value={option}
              >
                {option}
              </option>
            ))}
          </select>

          <select
            value={category}
            onChange={(event) =>
              setCategory(event.target.value)
            }
          >
            <option value="">
              All categories
            </option>

            {categoryOptions.map((option) => (
              <option
                key={option}
                value={option}
              >
                {option}
              </option>
            ))}
          </select>

          <select
            value={slaBreached}
            onChange={(event) =>
              setSlaBreached(event.target.value)
            }
          >
            <option value="">
              All SLA
            </option>

            <option value="false">
              Within SLA
            </option>

            <option value="true">
              SLA breached
            </option>
          </select>
        </section>

        {/* CLEAR FILTERS */}

        {hasFilters && (
          <button
            className="clear-ticket-filters"
            onClick={clearFilters}
          >
            Clear filters
          </button>
        )}

        {/* RESULT HEADER */}

        <div className="ticket-list-result-header">
          <div>
            <strong>{totalElements}</strong>{" "}
            {totalElements === 1
              ? "ticket"
              : "tickets"}
          </div>

          {totalPages > 0 && (
            <span>
              Page {page + 1} of{" "}
              {totalPages}
            </span>
          )}
        </div>

        {/* ERROR */}

        {error && (
          <div className="ticket-list-error">
            <span>{error}</span>

            <button
              onClick={() =>
                loadTickets(page)
              }
            >
              Retry
            </button>
          </div>
        )}

        {/* LOADING */}

        {loading && (
          <div className="ticket-list-loading">
            <div />
            <div />
            <div />
          </div>
        )}

        {/* EMPTY */}

        {!loading &&
          !error &&
          filteredTickets.length === 0 && (
            <div className="ticket-list-empty">
              <div className="ticket-empty-icon">
                ⌁
              </div>

              <h2>No tickets found</h2>

              <p>
                {hasFilters
                  ? "Try changing your filters or search query."
                  : "There are no tickets available yet."}
              </p>

              {hasFilters && (
                <button
                  className="secondary-button"
                  onClick={clearFilters}
                >
                  Clear filters
                </button>
              )}
            </div>
          )}

        {/* TICKET LIST */}

        {!loading &&
          !error &&
          filteredTickets.length > 0 && (
            <section className="ticket-list">
              {filteredTickets.map(
                (ticket) => (
                  <article
                    className="ticket-list-row"
                    key={ticket.id}
                    onClick={() =>
                      navigate(
                        `/tickets/${ticket.id}`
                      )
                    }
                  >
                    <div className="ticket-list-main">
                      <div className="ticket-list-topline">
                        <span className="ticket-list-id">
                          #{ticket.id}
                        </span>

                        <span
                          className={`ticket-priority ticket-priority-${ticket.priority.toLowerCase()}`}
                        >
                          {ticket.priority}
                        </span>

                        {ticket.slaBreached && (
                          <span className="ticket-sla-breached">
                            SLA BREACHED
                          </span>
                        )}
                      </div>

                      <h2>
                        {ticket.title}
                      </h2>

                      <p>
                        {ticket.description}
                      </p>

                      <div className="ticket-list-meta">
                        <span>
                          {ticket.category}
                        </span>

                        <span>•</span>

                        <span>
                          {ticket.assignedAgentId
                            ? "Assigned"
                            : "Unassigned"}
                        </span>

                        <span>•</span>

                        <span>
                          Created{" "}
                          {formatDate(
                            ticket.createdAt
                          )}
                        </span>
                      </div>
                    </div>

                    <div className="ticket-list-side">
                      <span
                        className={`ticket-status ticket-status-${ticket.status
                          .toLowerCase()
                          .replaceAll(
                            "_",
                            "-"
                          )}`}
                      >
                        {formatStatus(
                          ticket.status
                        )}
                      </span>

                      <span
                        className={
                          ticket.slaBreached
                            ? "ticket-sla danger"
                            : "ticket-sla"
                        }
                      >
                        {getSlaLabel(ticket)}
                      </span>

                      <span className="ticket-updated">
                        Updated{" "}
                        {formatDateTime(
                          ticket.updatedAt
                        )}
                      </span>
                    </div>

                    <span className="ticket-row-arrow">
                      →
                    </span>
                  </article>
                )
              )}
            </section>
          )}

        {/* PAGINATION */}

        {!loading &&
          !error &&
          totalPages > 1 && (
            <div className="ticket-pagination">
              <button
                className="secondary-button"
                disabled={page === 0}
                onClick={() =>
                  handlePageChange(page - 1)
                }
              >
                ← Previous
              </button>

              <div className="ticket-page-numbers">
                {pageNumbers.map(
                  (pageNumber) => (
                    <button
                      key={pageNumber}
                      className={
                        pageNumber === page
                          ? "active"
                          : ""
                      }
                      onClick={() =>
                        handlePageChange(
                          pageNumber
                        )
                      }
                    >
                      {pageNumber + 1}
                    </button>
                  )
                )}
              </div>

              <button
                className="secondary-button"
                disabled={
                  page === totalPages - 1
                }
                onClick={() =>
                  handlePageChange(page + 1)
                }
              >
                Next →
              </button>
            </div>
          )}
      </main>
    </div>
  );
}