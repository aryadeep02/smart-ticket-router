import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

import { useAuth } from "../context/AuthContext";

export default function OAuth2Callback() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { loginWithToken } = useAuth();

  useEffect(() => {
    const token = searchParams.get("token");

    if (!token) {
      navigate("/login?error=google-login-failed", {
        replace: true,
      });
      return;
    }

    const completeLogin = async () => {
      try {
        await loginWithToken(token);

        navigate("/dashboard", {
          replace: true,
        });
      } catch {
        navigate("/login?error=google-login-failed", {
          replace: true,
        });
      }
    };

    completeLogin();
  }, [loginWithToken, navigate, searchParams]);

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2>Signing you in...</h2>
        <p>Completing Google authentication.</p>
      </div>
    </div>
  );
}