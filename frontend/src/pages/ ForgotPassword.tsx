import { type FormEvent, useState } from "react";
import { Link } from "react-router-dom";
import api from "../services/api";

export default function ForgotPassword() {
    const [email, setEmail] = useState("");
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();

        setMessage("");
        setError("");
        setLoading(true);

        try {
            const response = await api.post(
                "/auth/forgot-password",
                { email }
            );

            setMessage(
                response.data.message ||
                    "If an account exists for this email, a password reset link has been sent."
            );
        } catch (err: any) {
            setError(
                err?.response?.data?.message ||
                    "Unable to process the password reset request."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-page">
            <div className="auth-card">

                <div className="auth-icon">
                    🔐
                </div>

                <div className="auth-heading">
                    <h2>Forgot your password?</h2>

                    <p>
                        No worries. Enter your email and we'll
                        send you a secure link to reset it.
                    </p>
                </div>

                <form onSubmit={handleSubmit}>

                <div className="form-field">
                        <label htmlFor="email">
                            Email address
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

                    {message && (
                        <div className="success-message">
                            {message}
                        </div>
                    )}

                    {error && (
                        <div className="error-message">
                            {error}
                        </div>
                    )}

                    <button
                        type="submit"
                       className="auth-submit primary-button"
                        disabled={loading}
                    >
                        {loading ? (
                            <>
                                <span className="button-spinner" />
                                Sending link...
                            </>
                        ) : (
                            "Send Reset Link"
                        )}
                    </button>
                </form>

                <div className="auth-footer">
                    <span>Remember your password?</span>{" "}
                    <Link to="/login">
                        Back to Login
                    </Link>
                </div>

            </div>
        </div>
    );
}