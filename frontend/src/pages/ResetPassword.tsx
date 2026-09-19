import {
    type FormEvent,
    useState,
} from "react";

import {
    Link,
    useNavigate,
    useSearchParams,
} from "react-router-dom";

import api from "../services/api";

export default function ResetPassword() {

    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    const token = searchParams.get("token");

    const [newPassword, setNewPassword] =
        useState("");

    const [confirmPassword, setConfirmPassword] =
        useState("");

    const [message, setMessage] =
        useState("");

    const [error, setError] =
        useState("");

    const [loading, setLoading] =
        useState(false);

    const handleSubmit = async (
        event: FormEvent
    ) => {

        event.preventDefault();

        setMessage("");
        setError("");

        if (!token) {
            setError(
                "Invalid or missing password reset link."
            );
            return;
        }

        if (newPassword !== confirmPassword) {
            setError(
                "Passwords do not match."
            );
            return;
        }

        setLoading(true);

        try {

            const response = await api.post(
                "/auth/reset-password",
                {
                    token,
                    newPassword,
                }
            );

            setMessage(
                response.data.message ||
                "Password reset successfully."
            );

            setTimeout(() => {
                navigate("/login");
            }, 1500);

        } catch (err: any) {

            setError(
                err?.response?.data?.message ||
                "Unable to reset your password."
            );

        } finally {

            setLoading(false);
        }
    };

    return (
        <div className="auth-page">
            <div className="auth-card">

                <h2>Reset Password</h2>

                <p>
                    Enter your new password below.
                </p>

                {!token ? (
                    <>
                        <p className="error-message">
                            Invalid or missing password reset link.
                        </p>

                        <Link
                            to="/forgot-password"
                            className="auth-button"
                        >
                            Request a New Link
                        </Link>
                    </>
                ) : (

                    <form onSubmit={handleSubmit}>

                        <div className="form-group">

                            <label htmlFor="newPassword">
                                New Password
                            </label>

                            <input
                                id="newPassword"
                                type="password"
                                value={newPassword}
                                onChange={(event) =>
                                    setNewPassword(
                                        event.target.value
                                    )
                                }
                                placeholder="Enter new password"
                                minLength={8}
                                maxLength={100}
                                required
                            />

                        </div>

                        <div className="form-group">

                            <label htmlFor="confirmPassword">
                                Confirm Password
                            </label>

                            <input
                                id="confirmPassword"
                                type="password"
                                value={confirmPassword}
                                onChange={(event) =>
                                    setConfirmPassword(
                                        event.target.value
                                    )
                                }
                                placeholder="Confirm new password"
                                minLength={8}
                                maxLength={100}
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
                                ? "Resetting..."
                                : "Reset Password"}
                        </button>

                    </form>
                )}

                <p>
                    <Link to="/login">
                        Back to Login
                    </Link>
                </p>

            </div>
        </div>
    );
}