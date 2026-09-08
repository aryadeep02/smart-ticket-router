import { useEffect, useRef, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import api from "../services/api";

export default function VerifyEmail() {
    const [searchParams] = useSearchParams();

    const hasVerified = useRef(false);

    const [status, setStatus] = useState<
        "verifying" | "success" | "error"
    >("verifying");

    const [message, setMessage] = useState(
        "Verifying your email address..."
    );

    useEffect(() => {
        if (hasVerified.current) {
            return;
        }

        const token = searchParams.get("token");

        if (!token) {
            setStatus("error");
            setMessage("Invalid verification link.");
            return;
        }

        hasVerified.current = true;

        const verifyEmail = async () => {
            try {
                const response = await api.get(
                    "/auth/verify-email",
                    {
                        params: { token },
                    }
                );

                setStatus("success");
                setMessage(
                    response.data.message ||
                    "Email verified successfully."
                );

            } catch (error: any) {
                setStatus("error");

                setMessage(
                    error?.response?.data?.message ||
                    "This verification link is invalid or has expired."
                );
            }
        };

        verifyEmail();
    }, [searchParams]);

    return (
        <div className="auth-page">
            <div className="auth-card">

                {status === "verifying" && (
                    <>
                        <h2>Verifying your email...</h2>
                        <p>{message}</p>
                    </>
                )}

                {status === "success" && (
                    <>
                        <h2>Email Verified ✅</h2>

                        <p>{message}</p>

                        <Link
                            to="/login"
                            className="auth-button"
                        >
                            Continue to Login
                        </Link>
                    </>
                )}

                {status === "error" && (
                    <>
                        <h2>Verification Failed</h2>

                        <p>{message}</p>

                        <Link
                            to="/login"
                            className="auth-button"
                        >
                            Back to Login
                        </Link>
                    </>
                )}

            </div>
        </div>
    );
}