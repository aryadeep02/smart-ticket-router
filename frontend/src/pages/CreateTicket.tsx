import { useState } from "react";
import type { FormEvent } from "react";
import { useNavigate } from "react-router-dom";

import Navbar from "../components/Navbar";
import { createTicket } from "../services/ticketService";

export default function CreateTicket() {
  const navigate = useNavigate();

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    const trimmedTitle = title.trim();
    const trimmedDescription = description.trim();

    if (!trimmedTitle || !trimmedDescription) {
      setError(
        "Title and description cannot be empty."
      );
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
    } catch (error: any) {
      setError(
        error.response?.data?.message ||
          "Failed to create ticket"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <Navbar />

      <main className="page-container">
        <button
          className="secondary-button"
          type="button"
          onClick={() => navigate("/dashboard")}
        >
          ← Back to Dashboard
        </button>

        <div className="form-card">
          <div className="page-header">
            <h1>Create Ticket</h1>

            <p>
              Describe your issue and our intelligent
              router will automatically categorize and
              prioritize it.
            </p>
          </div>

          <form
            className="ticket-form"
            onSubmit={handleSubmit}
          >
            <div className="form-field">
              <label htmlFor="title">
                Title
              </label>

              <input
                id="title"
                type="text"
                value={title}
                onChange={(event) =>
                  setTitle(event.target.value)
                }
                placeholder="Briefly describe your issue"
                maxLength={255}
                required
              />
            </div>

            <div className="form-field">
              <label htmlFor="description">
                Description
              </label>

              <textarea
                id="description"
                value={description}
                onChange={(event) =>
                  setDescription(event.target.value)
                }
                placeholder="Describe your issue in detail..."
                rows={10}
                required
              />

              <small>
                {description.length} / 5000 characters
              </small>
            </div>

            {error && (
              <div className="form-error">
                {error}
              </div>
            )}

            <div className="form-actions">
              <button
                className="secondary-button"
                type="button"
                onClick={() => navigate("/dashboard")}
              >
                Cancel
              </button>

              <button
                className="primary-button"
                type="submit"
                disabled={
                  loading ||
                  !title.trim() ||
                  !description.trim()
                }
              >
                {loading
                  ? "Creating..."
                  : "Create Ticket"}
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}