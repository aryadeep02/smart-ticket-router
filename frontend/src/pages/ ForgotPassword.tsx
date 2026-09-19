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

                <h2>Forgot Password?</h2>

                <p>
                    Enter your email address and we'll send you
                    a password reset link.
                </p>

                <form onSubmit={handleSubmit}>

                    <div className="form-group">
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
                            required
                        />
                    </div>

                    {message && (
                        <p className="success-message">
                            {message}
                        </p>
                    )}

                    {error && (
                        <p className="error-message">
                            {error}
                        </p>
                    )}

                    <button
                        type="submit"
                        className="auth-button"
                        disabled={loading}
                    >
                        {loading
                            ? "Sending..."
                            : "Send Reset Link"}
                    </button>

                </form>

                <p>
                    Remember your password?{" "}
                    <Link to="/login">
                        Back to Login
                    </Link>
                </p>

            </div>
        </div>
    );
}