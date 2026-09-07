import { useNavigate } from "react-router-dom";

import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <header className="navbar">
      <div
        className="navbar-brand"
        onClick={() => navigate("/dashboard")}
        role="button"
        tabIndex={0}
        onKeyDown={(event) => {
          if (event.key === "Enter") {
            navigate("/dashboard");
          }
        }}
      >
        Smart Ticket Router
      </div>

      <nav className="navbar-actions">
        <button
          className="nav-button"
          onClick={() => navigate("/dashboard")}
        >
          Dashboard
        </button>

        {user?.role === "CUSTOMER" && (
          <button
            className="nav-button"
            onClick={() => navigate("/tickets/create")}
          >
            Create Ticket
          </button>
        )}

        {user?.role === "ADMIN" && (
          <>
            <button
              className="nav-button"
              onClick={() => navigate("/admin/users")}
            >
              Users
            </button>

            <button
              className="nav-button"
              onClick={() => navigate("/admin/teams")}
            >
              Teams
            </button>
          </>
        )}

        <span className="navbar-user">
          {user?.name}{" "}
          <span className="navbar-role">
            ({user?.role})
          </span>
        </span>

        <button
          className="logout-button"
          onClick={handleLogout}
        >
          Logout
        </button>
      </nav>
    </header>
  );
}