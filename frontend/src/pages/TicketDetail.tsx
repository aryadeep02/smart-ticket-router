import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import Navbar from "../components/Navbar";
import { useAuth } from "../context/AuthContext";

import {
  assignAgent,
  getTicket,
  updateTicketStatus,
} from "../services/ticketService";

import { addComment, getComments } from "../services/commentService";

import { getAgents } from "../services/adminService";

import type { ChangeEvent } from "react";
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

    setCommentError("");
    setCommentLoading(true);

    try {
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

  const handleStatusChange = async (event: ChangeEvent<HTMLSelectElement>) => {
    if (!ticket) {
      return;
    }

    const newStatus = event.target.value;

    setStatusError("");
    setStatusUpdating(true);

    try {
      const updatedTicket = await updateTicketStatus(ticket.id, newStatus);

      setTicket(updatedTicket);
    } catch (error: any) {
      setStatusError(
        error.response?.data?.message || "Failed to update ticket status"
      );
    } finally {
      setStatusUpdating(false);
    }
  };

  const handleAssignAgent = async () => {
    if (!ticket || !selectedAgentId) {
      return;
    }

    setAssignmentError("");
    setAssignmentUpdating(true);

    try {
      const updatedTicket = await assignAgent(
        ticket.id,
        Number(selectedAgentId)
      );

      setTicket(updatedTicket);
      setSelectedAgentId("");
    } catch (error: any) {
      setAssignmentError(
        error.response?.data?.message || "Failed to assign agent"
      );
    } finally {
      setAssignmentUpdating(false);
    }
  };

  if (loading) {
    return (
      <div>
        <Navbar />

        <main className="page-container">
          <div className="detail-state">
            <p>Loading ticket...</p>
          </div>
        </main>
      </div>
    );
  }

  if (error || !ticket) {
    return (
      <div>
        <Navbar />

        <main className="page-container">
          <div className="detail-state">
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
      </div>
    );
  }

  return (
    <div>
      <Navbar />

      <main className="page-container">
        <button
          className="secondary-button back-button"
          onClick={() => navigate("/dashboard")}
        >
          ← Back to Dashboard
        </button>

        <div className="ticket-detail">
          <section className="ticket-detail-header">
            <div>
              <div className="ticket-id">Ticket #{ticket.id}</div>

              <h1>{ticket.title}</h1>

              <p className="ticket-detail-description">{ticket.description}</p>
            </div>

            <span
              className={`priority-badge priority-${ticket.priority.toLowerCase()}`}
            >
              {ticket.priority}
            </span>
          </section>

          <section className="detail-grid">
            <div className="detail-item">
              <span>Category</span>
              <strong>{ticket.category}</strong>
            </div>

            <div className="detail-item">
              <span>Status</span>

              {user?.role === "AGENT" || user?.role === "ADMIN" ? (
                <>
                  <select
                    className="filter-select"
                    value={ticket.status}
                    onChange={handleStatusChange}
                    disabled={statusUpdating}
                  >
                    <option value="OPEN">OPEN</option>

                    <option value="IN_PROGRESS">IN_PROGRESS</option>

                    <option value="WAITING_FOR_CUSTOMER">
                      WAITING_FOR_CUSTOMER
                    </option>

                    <option value="RESOLVED">RESOLVED</option>

                    <option value="CLOSED">CLOSED</option>
                  </select>

                  {statusUpdating && <small>Updating...</small>}

                  {statusError && (
                    <small className="form-error-text">{statusError}</small>
                  )}
                </>
              ) : (
                <strong>{ticket.status}</strong>
              )}
            </div>

            <div className="detail-item">
              <span>AI Confidence</span>

              <strong>
                {ticket.aiConfidence !== null
                  ? `${(ticket.aiConfidence * 100).toFixed(1)}%`
                  : "N/A"}
              </strong>
            </div>

            <div className="detail-item">
              <span>Classification</span>

              <strong>{ticket.aiClassificationStatus}</strong>
            </div>

            <div className="detail-item">
              <span>SLA</span>

              <strong
                className={ticket.slaBreached ? "sla-breached" : "sla-ok"}
              >
                {ticket.slaBreached ? "Breached" : "Within SLA"}
              </strong>
            </div>

            <div className="detail-item">
              <span>SLA Deadline</span>

              <strong>{new Date(ticket.slaDeadline).toLocaleString()}</strong>
            </div>

            <div className="detail-item">
              <span>Created</span>

              <strong>{new Date(ticket.createdAt).toLocaleString()}</strong>
            </div>

            <div className="detail-item">
              <span>Assigned Agent</span>

              <strong>
                {ticket.assignedAgentId
                  ? `Agent #${ticket.assignedAgentId}`
                  : "Unassigned"}
              </strong>
            </div>

            {user?.role === "ADMIN" && (
              <div className="detail-item">
                <span>Assign Agent</span>

                <select
                  className="filter-select"
                  value={selectedAgentId}
                  onChange={(event) => setSelectedAgentId(event.target.value)}
                  disabled={assignmentUpdating}
                >
                  <option value="">Select an agent</option>

                  {agents
                    .filter(
                      (agent) => agent.supportTeamId === ticket.supportTeamId
                    )
                    .map((agent) => (
                      <option key={agent.id} value={agent.id}>
                        {agent.name}
                      </option>
                    ))}
                </select>
                {agents.filter(
                  (agent) => agent.supportTeamId === ticket.supportTeamId
                ).length === 0 && (
                  <small>No agents available for this support team.</small>
                )}

                <button
                  className="primary-button"
                  onClick={handleAssignAgent}
                  disabled={assignmentUpdating || !selectedAgentId}
                >
                  {assignmentUpdating ? "Assigning..." : "Assign Agent"}
                </button>

                {assignmentError && (
                  <small className="form-error-text">{assignmentError}</small>
                )}
              </div>
            )}
          </section>

          <section className="comments-section">
            <div className="section-header">
              <h2>Comments</h2>

              <span>
                {comments.length}{" "}
                {comments.length === 1 ? "comment" : "comments"}
              </span>
            </div>

            {comments.length === 0 && (
              <div className="empty-state">
                <p>No comments yet.</p>
              </div>
            )}

            {comments.length > 0 && (
              <div className="comment-list">
                {comments.map((comment) => (
                  <article className="comment-card" key={comment.id}>
                    <div className="comment-header">
                      <strong>{comment.authorName}</strong>

                      <small>
                        {new Date(comment.createdAt).toLocaleString()}
                      </small>
                    </div>

                    <p>{comment.content}</p>
                  </article>
                ))}
              </div>
            )}

            <div className="comment-form">
              <textarea
                value={commentText}
                onChange={(event) => setCommentText(event.target.value)}
                placeholder="Write a comment..."
                maxLength={5000}
                rows={5}
              />

              <div className="comment-form-footer">
                <small>{commentText.length} / 5000</small>

                {commentError && (
                  <span className="form-error-text">{commentError}</span>
                )}

                <button
                  className="primary-button"
                  onClick={handleAddComment}
                  disabled={commentLoading || !commentText.trim()}
                >
                  {commentLoading ? "Posting..." : "Add Comment"}
                </button>
              </div>
            </div>
          </section>
        </div>
      </main>
    </div>
  );
}
