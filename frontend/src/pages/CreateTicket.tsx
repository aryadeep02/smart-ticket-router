import { useState } from "react";
import { useNavigate } from "react-router-dom";

import Navbar from "../components/Navbar";
import { createTicket } from "../services/ticketService";

export default function CreateTicket() {
  const navigate = useNavigate();

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (event: {
    preventDefault: () => void;
  }) => {
    event.preventDefault();

    if (loading) {
      return;
    }

    const trimmedTitle = title.trim();
    const trimmedDescription = description.trim();

    if (!trimmedTitle || !trimmedDescription) {
      setError("Title and description cannot be empty.");
      return;
    }

    if (trimmedDescription.length > 5000) {
      setError("Description cannot exceed 5000 characters.");
      return;
    }

    setError("");
    setLoading(true);

    try {
      const ticket = await createTicket({
        title: trimmedTitle,
        description: trimmedDescription,
      });

      navigate(`/tickets/${ticket.id}`);
    } catch (error: unknown) {
      console.error("Failed to create ticket:", error);

      if (
        typeof error === "object" &&
        error !== null &&
        "response" in error
      ) {
        const response = (
          error as {
            response?: {
              data?: {
                message?: string;
              };
            };
          }
        ).response;

        setError(
          response?.data?.message ||
            "Failed to create ticket. Please try again."
        );
      } else {
        setError("Failed to create ticket. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Navbar />

      <main className="create-ticket-page">
        <button
          className="create-back-button"
          type="button"
          onClick={() => navigate("/dashboard")}
        >
          ← Dashboard
        </button>

        <div className="create-ticket-header">
          <div>
            <span className="eyebrow">Support request</span>

            <h1>Create a ticket</h1>

            <p>
              Tell us what happened. Our AI router will
              automatically classify your issue and send it
              to the right support team.
            </p>
          </div>
        </div>

        <div className="create-ticket-layout">
          <section className="create-ticket-card">
            <div className="create-card-heading">
              <div className="create-card-icon">
                +
              </div>

              <div>
                <h2>Describe your issue</h2>
                <p>
                  Give us enough detail so the support team
                  can help you quickly.
                </p>
              </div>
            </div>

            <form
              className="create-ticket-form"
              onSubmit={handleSubmit}
            >
              <div className="create-field">
                <div className="field-label-row">
                  <label htmlFor="title">
                    Issue title
                  </label>

                  <span>
                    {title.length}/255
                  </span>
                </div>

                <input
                  id="title"
                  type="text"
                  value={title}
                  onChange={(event) =>
                    setTitle(event.target.value)
                  }
                  placeholder="e.g. Payment failed but money was deducted"
                  maxLength={255}
                  required
                />
              </div>

              <div className="create-field">
                <div className="field-label-row">
                  <label htmlFor="description">
                    Description
                  </label>

                  <span>
                    {description.length}/5000
                  </span>
                </div>

                <textarea
                  id="description"
                  value={description}
                  onChange={(event) =>
                    setDescription(event.target.value)
                  }
                  placeholder={
                    "Explain what happened, what you expected, and any relevant details..."
                  }
                  rows={9}
                  maxLength={5000}
                  required
                />
              </div>

              {error && (
                <div className="create-form-error" role="alert">
                  <span>!</span>
                  <p>{error}</p>
                </div>
              )}

              <div className="create-form-actions">
                <button
                  className="create-cancel-button"
                  type="button"
                  onClick={() => navigate("/dashboard")}
                  disabled={loading}
                >
                  Cancel
                </button>

                <button
                  className="create-submit-button"
                  type="submit"
                  disabled={
                    loading ||
                    !title.trim() ||
                    !description.trim()
                  }
                >
                  {loading ? (
                    <>
                      <span className="create-spinner" />
                      Creating ticket...
                    </>
                  ) : (
                    <>
                      Create ticket
                      <span>→</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          </section>

          <aside className="routing-info-card">
            <div className="routing-icon">
              ✦
            </div>

            <span className="eyebrow">
              Smart routing
            </span>

            <h2>
              Let the system do the routing.
            </h2>

            <p>
              You don't need to choose a category or priority.
              Smart Ticket Router analyzes your request
              automatically.
            </p>

            <div className="routing-steps">
              <div className="routing-step">
                <span>01</span>

                <div>
                  <strong>Understand</strong>
                  <p>
                    Your title and description are analyzed.
                  </p>
                </div>
              </div>

              <div className="routing-step">
                <span>02</span>

                <div>
                  <strong>Classify</strong>
                  <p>
                    AI predicts the category and priority.
                  </p>
                </div>
              </div>

              <div className="routing-step">
                <span>03</span>

                <div>
                  <strong>Route</strong>
                  <p>
                    Your ticket is sent to the appropriate
                    support team.
                  </p>
                </div>
              </div>
            </div>

            <div className="routing-note">
              <span>✓</span>
              <p>
                If AI classification is unavailable, the
                system automatically falls back to a safe
                default.
              </p>
            </div>
          </aside>
        </div>
      </main>
    </>
  );
}