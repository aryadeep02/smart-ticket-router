import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import type { ChangeEvent } from "react";

import Navbar from "../components/Navbar";
import { useAuth } from "../context/AuthContext";

import {
  assignAgent,
  getTicket,
  updateTicketStatus,
} from "../services/ticketService";

import { addComment, getComments } from "../services/commentService";
import { getAgents } from "../services/adminService";

import type { Ticket } from "../types/ticket";
import type { TicketComment } from "../types/comment";
import type { AdminUser } from "../services/adminService";

export default function TicketDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [ticket, setTicket] = useState<Ticket | null>(null);
  const [comments, setComments] = useState<TicketComment[]>([]);
  const [agents, setAgents] = useState<AdminUser[]>([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [commentText, setCommentText] = useState("");
  const [commentLoading, setCommentLoading] = useState(false);
  const [commentError, setCommentError] = useState("");

  const [statusUpdating, setStatusUpdating] = useState(false);
  const [statusError, setStatusError] = useState("");

  const [selectedAgentId, setSelectedAgentId] = useState("");
  const [assignmentUpdating, setAssignmentUpdating] = useState(false);
  const [assignmentError, setAssignmentError] = useState("");

  useEffect(() => {
    const loadTicket = async () => {
      if (!id) {
        setError("Invalid ticket ID");
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError("");

        const ticketData = await getTicket(Number(id));
        setTicket(ticketData);

        const commentData = await getComments(Number(id));
        setComments(commentData);
      } catch {
        setError("Failed to load ticket");
      } finally {
        setLoading(false);
      }
    };

    loadTicket();
  }, [id]);

  useEffect(() => {
    const loadAgents = async () => {
      if (user?.role !== "ADMIN") {
        return;
      }

      try {
        const data = await getAgents();
        setAgents(data);
      } catch {
        setAssignmentError("Failed to load agents");
      }
    };

    loadAgents();
  }, [user?.role]);

  const handleAddComment = async () => {
    if (!id || !commentText.trim()) {
      return;
    }

    try {
      setCommentError("");
      setCommentLoading(true);

      const newComment = await addComment(Number(id), {
        content: commentText.trim(),
      });

      setComments((current) => [...current, newComment]);
      setCommentText("");
    } catch {
      setCommentError("Failed to add comment");
    } finally {
      setCommentLoading(false);
    }
  };

  const handleStatusChange = async (
    event: ChangeEvent<HTMLSelectElement>
  ) => {
    if (!ticket) {
      return;
    }

    const newStatus = event.target.value;

    try {
      setStatusError("");
      setStatusUpdating(true);

      const updatedTicket = await updateTicketStatus(
        ticket.id,
        newStatus
      );

      setTicket(updatedTicket);
    } catch (error: any) {
      setStatusError(
        error.response?.data?.message ||
          "Failed to update ticket status"
      );
    } finally {
      setStatusUpdating(false);
    }
  };

  const handleAssignAgent = async () => {
    if (!ticket || !selectedAgentId) {
      return;
    }

    try {
      setAssignmentError("");
      setAssignmentUpdating(true);

      const updatedTicket = await assignAgent(
        ticket.id,
        Number(selectedAgentId)
      );

      setTicket(updatedTicket);
      setSelectedAgentId("");
    } catch (error: any) {
      setAssignmentError(
        error.response?.data?.message ||
          "Failed to assign agent"
      );
    } finally {
      setAssignmentUpdating(false);
    }
  };

  if (loading) {
    return (
      <>
        <Navbar />

        <main className="ticket-detail-page">
          <div className="detail-loading">
            <div className="loading-spinner" />
            <p>Loading ticket...</p>
          </div>
        </main>
      </>
    );
  }

  if (error || !ticket) {
    return (
      <>
        <Navbar />

        <main className="ticket-detail-page">
          <div className="detail-error">
            <div className="detail-error-icon">!</div>

            <span className="eyebrow">Ticket unavailable</span>

            <h2>Unable to load ticket</h2>

            <p>{error || "Ticket not found"}</p>

            <button
              className="secondary-button"
              onClick={() => navigate("/dashboard")}
            >
              ← Back to Dashboard
            </button>
          </div>
        </main>
      </>
    );
  }

  const confidence =
    ticket.aiConfidence !== null
      ? ticket.aiConfidence * 100
      : null;

  const teamAgents = agents.filter(
    (agent) => agent.supportTeamId === ticket.supportTeamId
  );

  const isClosed = ticket.status === "CLOSED";

  return (
    <>
      <Navbar />

      <main className="ticket-detail-page">
        <button
          className="detail-back-button"
          onClick={() => navigate("/dashboard")}
        >
          ← Dashboard
        </button>

        <div className="ticket-detail-layout">
          <div className="ticket-main-column">
            <section className="ticket-hero">
              <div className="ticket-hero-top">
                <span className="detail-ticket-id">
                  TICKET #{ticket.id}
                </span>

                <span
                  className={`priority-badge priority-${ticket.priority.toLowerCase()}`}
                >
                  {ticket.priority}
                </span>
              </div>

              <h1>{ticket.title}</h1>

              <p className="ticket-hero-description">
                {ticket.description}
              </p>

              <div className="ticket-hero-meta">
                <span>
                  {ticket.category}
                </span>

                <span className="hero-dot">•</span>

                <span>
                  {ticket.status.replaceAll("_", " ")}
                </span>

                {ticket.slaBreached && (
                  <>
                    <span className="hero-dot">•</span>
                    <span className="sla-breached">
                      SLA BREACHED
                    </span>
                  </>
                )}
              </div>
            </section>

            <section className="ticket-info-grid">
              <div className="info-card">
                <span>Category</span>
                <strong>{ticket.category}</strong>
              </div>

              <div className="info-card">
                <span>Priority</span>

                <strong
                  className={`priority-text priority-text-${ticket.priority.toLowerCase()}`}
                >
                  {ticket.priority}
                </strong>
              </div>

              <div className="info-card">
                <span>Support team</span>
                <strong>
                  Team #{ticket.supportTeamId}
                </strong>
              </div>

              <div className="info-card">
                <span>Assigned agent</span>
                <strong>
                  {ticket.assignedAgentId
                    ? `Agent #${ticket.assignedAgentId}`
                    : "Unassigned"}
                </strong>
              </div>

              <div className="info-card">
                <span>Created</span>
                <strong>
                  {new Date(
                    ticket.createdAt
                  ).toLocaleString()}
                </strong>
              </div>

              <div className="info-card">
                <span>SLA deadline</span>

                <strong
                  className={
                    ticket.slaBreached
                      ? "danger-text"
                      : ""
                  }
                >
                  {new Date(
                    ticket.slaDeadline
                  ).toLocaleString()}
                </strong>
              </div>
            </section>

            <section className="workflow-card">
              <div className="section-header">
                <div>
                  <span className="eyebrow">
                    Workflow
                  </span>

                  <h2>Ticket status</h2>
                </div>

                <span
                  className={`workflow-status status-${ticket.status
                    .toLowerCase()
                    .replaceAll("_", "-")}`}
                >
                  {ticket.status.replaceAll("_", " ")}
                </span>
              </div>

              {user?.role === "AGENT" ||
              user?.role === "ADMIN" ? (
                <div className="workflow-control">
                  <select
                    value={ticket.status}
                    onChange={handleStatusChange}
                    disabled={statusUpdating || isClosed}
                  >
                    <option value="OPEN">OPEN</option>
                    <option value="IN_PROGRESS">
                      IN PROGRESS
                    </option>
                    <option value="WAITING_FOR_CUSTOMER">
                      WAITING FOR CUSTOMER
                    </option>
                    <option value="RESOLVED">
                      RESOLVED
                    </option>
                    <option value="CLOSED">
                      CLOSED
                    </option>
                  </select>

                  {statusUpdating && (
                    <span className="inline-loading">
                      Updating...
                    </span>
                  )}
                </div>
              ) : (
                <p className="workflow-readonly">
                  Only support agents and administrators can
                  update ticket status.
                </p>
              )}

              {statusError && (
                <div className="inline-error">
                  {statusError}
                </div>
              )}
            </section>

            <section className="ai-card">
              <div className="ai-header">
                <div className="ai-icon">✦</div>

                <div>
                  <span className="eyebrow">
                    AI classification
                  </span>

                  <h2>Smart routing analysis</h2>
                </div>

                <span
                  className={`ai-status ai-${ticket.aiClassificationStatus.toLowerCase()}`}
                >
                  {ticket.aiClassificationStatus}
                </span>
              </div>

              <div className="ai-content">
                <div className="ai-result">
                  <span>Detected category</span>
                  <strong>{ticket.category}</strong>
                </div>

                <div className="ai-result">
                  <span>Detected priority</span>
                  <strong>{ticket.priority}</strong>
                </div>

                <div className="ai-confidence">
                  <div className="ai-confidence-header">
                    <span>Confidence</span>

                    <strong>
                      {confidence !== null
                        ? `${confidence.toFixed(1)}%`
                        : "N/A"}
                    </strong>
                  </div>

                  <div className="confidence-track">
                    <div
                      className="confidence-fill"
                      style={{
                        width:
                          confidence !== null
                            ? `${confidence}%`
                            : "0%",
                      }}
                    />
                  </div>
                </div>
              </div>
            </section>

            <section className="comments-section">
              <div className="section-header">
                <div>
                  <span className="eyebrow">
                    Conversation
                  </span>

                  <h2>Comments</h2>
                </div>

                <span className="comment-count">
                  {comments.length}
                </span>
              </div>

              {comments.length === 0 ? (
                <div className="comments-empty">
                  <div>💬</div>

                  <h3>No comments yet</h3>

                  <p>
                    Start the conversation with the customer
                    or support team.
                  </p>
                </div>
              ) : (
                <div className="comment-list">
                  {comments.map((comment) => (
                    <article
                      className="comment-card"
                      key={comment.id}
                    >
                      <div className="comment-avatar">
                        {comment.authorName
                          .charAt(0)
                          .toUpperCase()}
                      </div>

                      <div className="comment-content">
                        <div className="comment-header">
                          <strong>
                            {comment.authorName}
                          </strong>

                          <small>
                            {new Date(
                              comment.createdAt
                            ).toLocaleString()}
                          </small>
                        </div>

                        <p>{comment.content}</p>
                      </div>
                    </article>
                  ))}
                </div>
              )}

              <div className="comment-form">
                <textarea
                  value={commentText}
                  onChange={(event) =>
                    setCommentText(event.target.value)
                  }
                  placeholder="Write a reply..."
                  maxLength={5000}
                  rows={4}
                />

                <div className="comment-form-footer">
                  <small>
                    {commentText.length} / 5000
                  </small>

                  {commentError && (
                    <span className="form-error-text">
                      {commentError}
                    </span>
                  )}

                  <button
                    className="primary-button"
                    onClick={handleAddComment}
                    disabled={
                      commentLoading ||
                      !commentText.trim()
                    }
                  >
                    {commentLoading
                      ? "Posting..."
                      : "Send reply"}
                  </button>
                </div>
              </div>
            </section>
          </div>

          <aside className="ticket-side-column">
            <section className="side-card sla-card">
              <div className="side-card-header">
                <span className="eyebrow">
                  Service level
                </span>

                <span
                  className={
                    ticket.slaBreached
                      ? "sla-indicator breached"
                      : "sla-indicator healthy"
                  }
                >
                  <span />
                  {ticket.slaBreached
                    ? "Breached"
                    : "Within SLA"}
                </span>
              </div>

              <div className="sla-main">
                <strong>
                  {ticket.slaBreached
                    ? "Attention required"
                    : "SLA is healthy"}
                </strong>

                <p>
                  Deadline{" "}
                  {new Date(
                    ticket.slaDeadline
                  ).toLocaleString()}
                </p>
              </div>
            </section>

            {user?.role === "ADMIN" && (
              <section className="side-card">
                <div className="side-card-header">
                  <div>
                    <span className="eyebrow">
                      Administration
                    </span>

                    <h3>Assignment</h3>
                  </div>
                </div>

                <div className="assignment-current">
                  <span>Current agent</span>

                  <strong>
                    {ticket.assignedAgentId
                      ? `Agent #${ticket.assignedAgentId}`
                      : "Unassigned"}
                  </strong>
                </div>

                <select
                  className="assignment-select"
                  value={selectedAgentId}
                  onChange={(event) =>
                    setSelectedAgentId(event.target.value)
                  }
                  disabled={assignmentUpdating}
                >
                  <option value="">
                    Select an agent
                  </option>

                  {teamAgents.map((agent) => (
                    <option
                      key={agent.id}
                      value={agent.id}
                    >
                      {agent.name}
                    </option>
                  ))}
                </select>

                {teamAgents.length === 0 && (
                  <p className="side-help">
                    No agents are available for this
                    support team.
                  </p>
                )}

                <button
                  className="primary-button full-width"
                  onClick={handleAssignAgent}
                  disabled={
                    assignmentUpdating ||
                    !selectedAgentId
                  }
                >
                  {assignmentUpdating
                    ? "Assigning..."
                    : "Assign agent"}
                </button>

                {assignmentError && (
                  <div className="inline-error">
                    {assignmentError}
                  </div>
                )}
              </section>
            )}

            <section className="side-card">
              <div className="side-card-header">
                <div>
                  <span className="eyebrow">
                    Ticket details
                  </span>

                  <h3>Information</h3>
                </div>
              </div>

              <div className="side-detail-list">
                <div>
                  <span>Customer</span>
                  <strong>
                    #{ticket.customerId}
                  </strong>
                </div>

                <div>
                  <span>Ticket ID</span>
                  <strong>#{ticket.id}</strong>
                </div>

                <div>
                  <span>Classification</span>
                  <strong>
                    {ticket.aiClassificationStatus}
                  </strong>
                </div>

                {ticket.resolvedAt && (
                  <div>
                    <span>Resolved</span>
                    <strong>
                      {new Date(
                        ticket.resolvedAt
                      ).toLocaleString()}
                    </strong>
                  </div>
                )}
              </div>
            </section>
          </aside>
        </div>
      </main>
    </>
  );
}