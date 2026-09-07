import { useState } from "react";
import type { FormEvent } from "react";
import { useNavigate } from "react-router-dom";

import api from "../services/api";


export default function Register() {
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    const trimmedName = name.trim();
    const trimmedEmail = email.trim();

    if (!trimmedName || !trimmedEmail || !password) {
      setError("Name, email and password are required.");
      return;
    }

    if (password.length < 8) {
      setError("Password must be at least 8 characters.");
      return;
    }

    setError("");
    setLoading(true);

    try {
      await api.post("/auth/register", {
        name: trimmedName,
        email: trimmedEmail,
        password,
      });

      navigate("/login");
    } catch (error: any) {
      setError(
        error.response?.data?.message ||
          "Failed to create account"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <h1>Create Account</h1>

          <p>
            Create your customer account to submit and
            track support tickets.
          </p>
        </div>

        <form
          className="auth-form"
          onSubmit={handleSubmit}
        >
          <div className="form-field">
            <label htmlFor="name">
              Name
            </label>

            <input
              id="name"
              type="text"
              value={name}
              onChange={(event) =>
                setName(event.target.value)
              }
              placeholder="Your name"
              autoComplete="name"
              required
            />
          </div>

          <div className="form-field">
            <label htmlFor="email">
              Email
            </label>

            <input
              id="email"
              type="email"
              value={email}
              onChange={(event) =>
                setEmail(event.target.value)
              }
              placeholder="you@example.com"
              autoComplete="email"
              required
            />
          </div>

          <div className="form-field">
            <label htmlFor="password">
              Password
            </label>

            <input
              id="password"
              type="password"
              value={password}
              onChange={(event) =>
                setPassword(event.target.value)
              }
              placeholder="Minimum 8 characters"
              autoComplete="new-password"
              required
            />
          </div>

          {error && (
            <div className="form-error">
              {error}
            </div>
          )}

          <button
            className="primary-button auth-submit"
            type="submit"
            disabled={loading}
          >
            {loading
              ? "Creating account..."
              : "Create Account"}
          </button>
        </form>

        <button
          className="secondary-button"
          type="button"
          onClick={() => navigate("/login")}
        >
          Already have an account? Sign in
        </button>
      </div>
    </main>
  );
}