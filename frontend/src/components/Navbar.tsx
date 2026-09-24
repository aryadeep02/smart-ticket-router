import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <header className="app-navbar">
      <div className="navbar-inner">
        <Link to="/dashboard" className="brand">
          <span className="brand-mark">
            ST
          </span>

          <span className="brand-text">
            Smart Ticket
          </span>
        </Link>

        <nav className="navbar-links">
          <Link to="/dashboard">Dashboard</Link>

          {user?.role === "ADMIN" && (
            <>
              <Link to="/admin/users">Users</Link>
              <Link to="/admin/teams">Teams</Link>
            </>
          )}
        </nav>

        <div className="navbar-actions">
          <Link
            to="/tickets/create"
            className="create-ticket-button"
          >
            <span>+</span>
            New ticket
          </Link>

          <div className="user-menu">
            <div className="user-avatar">
              {(user?.name?.charAt(0) || "U").toUpperCase()}
            </div>

            <div className="user-info">
              <strong>{user?.name || "User"}</strong>
              <span>{user?.role || "CUSTOMER"}</span>
            </div>
          </div>

          <button
            type="button"
            className="logout-button"
            onClick={handleLogout}
            aria-label="Logout"
            title="Logout"
          >
            ↗
          </button>
        </div>
      </div>
    </header>
  );
}