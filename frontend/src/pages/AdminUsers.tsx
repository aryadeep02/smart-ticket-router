import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import Navbar from "../components/Navbar";
import {
  createAdminUser,
  getAdminTeams,
  getAdminUsers,
} from "../services/adminService";

import type {
  AdminUser,
  SupportTeam,
} from "../services/adminService";

export default function AdminUsers() {
  const navigate = useNavigate();

  const [users, setUsers] = useState<AdminUser[]>([]);
  const [teams, setTeams] = useState<SupportTeam[]>([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [role, setRole] =
    useState<"CUSTOMER" | "AGENT" | "ADMIN">("CUSTOMER");

  const [supportTeamId, setSupportTeamId] = useState("");

  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState("");
  const [createSuccess, setCreateSuccess] = useState("");

  const [roleFilter, setRoleFilter] = useState("");
  const [teamFilter, setTeamFilter] = useState("");

  useEffect(() => {
    const loadData = async () => {
      try {
        const [userData, teamData] = await Promise.all([
          getAdminUsers(),
          getAdminTeams(),
        ]);

        setUsers(userData);
        setTeams(teamData);
      } catch {
        setError("Failed to load admin data");
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  const handleCreateUser = async () => {
    setCreateError("");
    setCreateSuccess("");

    if (!name.trim() || !email.trim() || !password) {
      setCreateError(
        "Name, email and password are required."
      );
      return;
    }

    if (password.length < 8) {
      setCreateError(
        "Password must be at least 8 characters."
      );
      return;
    }

    if (role === "AGENT" && !supportTeamId) {
      setCreateError(
        "Support team is required for an agent."
      );
      return;
    }

    setCreating(true);

    try {
      await createAdminUser({
        name: name.trim(),
        email: email.trim(),
        password,
        role,
        supportTeamId:
          role === "AGENT"
            ? Number(supportTeamId)
            : undefined,
      });

      setCreateSuccess(
        "User created successfully."
      );

      setName("");
      setEmail("");
      setPassword("");
      setRole("CUSTOMER");
      setSupportTeamId("");

      const updatedUsers = await getAdminUsers();
      setUsers(updatedUsers);
    } catch (error: any) {
      setCreateError(
        error.response?.data?.message ||
          "Failed to create user"
      );
    } finally {
      setCreating(false);
    }
  };

  const filteredUsers = users.filter((user) => {
    const matchesRole =
      !roleFilter || user.role === roleFilter;

    const matchesTeam =
      !teamFilter ||
      user.supportTeamId?.toString() === teamFilter;

    return matchesRole && matchesTeam;
  });

  return (
    <div>
      <Navbar />

      <main className="page-container">
        <button
          className="secondary-button back-button"
          onClick={() => navigate("/dashboard")}
        >
          ← Back to Dashboard
        </button>

        <div className="page-header">
          <h1>Users</h1>

          <p>
            Manage users, roles and support team assignments.
          </p>
        </div>

        <section className="form-card admin-create-card">
          <h2>Create User</h2>

          <div className="ticket-form">
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
                placeholder="Full name"
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
                placeholder="user@example.com"
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
                required
              />
            </div>

            <div className="form-field">
              <label htmlFor="role">
                Role
              </label>

              <select
                id="role"
                value={role}
                onChange={(event) =>
                  setRole(
                    event.target.value as
                      | "CUSTOMER"
                      | "AGENT"
                      | "ADMIN"
                  )
                }
              >
                <option value="CUSTOMER">
                  CUSTOMER
                </option>

                <option value="AGENT">
                  AGENT
                </option>

                <option value="ADMIN">
                  ADMIN
                </option>
              </select>
            </div>

            {role === "AGENT" && (
              <div className="form-field">
                <label htmlFor="supportTeam">
                  Support Team
                </label>

                <select
                  id="supportTeam"
                  value={supportTeamId}
                  onChange={(event) =>
                    setSupportTeamId(
                      event.target.value
                    )
                  }
                >
                  <option value="">
                    Select a support team
                  </option>

                  {teams.map((team) => (
                    <option
                      key={team.id}
                      value={team.id}
                    >
                      {team.name}
                    </option>
                  ))}
                </select>
              </div>
            )}

            {createError && (
              <div className="form-error">
                {createError}
              </div>
            )}

            {createSuccess && (
              <div className="form-success">
                {createSuccess}
              </div>
            )}

            <button
              className="primary-button"
              onClick={handleCreateUser}
              disabled={creating}
            >
              {creating
                ? "Creating..."
                : "Create User"}
            </button>
          </div>
        </section>

        <section>
          <div className="page-header">
            <h2>Existing Users</h2>

            <p>
              Filter users by role or support team.
            </p>
          </div>

          <div className="filters">
            <select
              className="filter-select"
              value={roleFilter}
              onChange={(event) =>
                setRoleFilter(event.target.value)
              }
            >
              <option value="">
                All Roles
              </option>

              <option value="CUSTOMER">
                CUSTOMER
              </option>

              <option value="AGENT">
                AGENT
              </option>

              <option value="ADMIN">
                ADMIN
              </option>
            </select>

            <select
              className="filter-select"
              value={teamFilter}
              onChange={(event) =>
                setTeamFilter(event.target.value)
              }
            >
              <option value="">
                All Teams
              </option>

              {teams.map((team) => (
                <option
                  key={team.id}
                  value={team.id}
                >
                  {team.name}
                </option>
              ))}
            </select>
          </div>

          {loading && (
            <p>Loading users...</p>
          )}

          {error && (
            <div className="form-error">
              {error}
            </div>
          )}

          {!loading && !error && (
            <>
              {filteredUsers.length === 0 && (
                <div className="empty-state">
                  <p>No users match the selected filters.</p>
                </div>
              )}

              <div className="admin-list">
                {filteredUsers.map((user) => (
                  <div
                    className="admin-card"
                    key={user.id}
                  >
                    <div className="admin-card-main">
                      <h3>{user.name}</h3>

                      <p>{user.email}</p>
                    </div>

                    <div className="admin-card-meta">
                      <span className="role-badge">
                        {user.role}
                      </span>

                      <span className="team-text">
                        {user.supportTeamName ??
                          "No team"}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}
        </section>
      </main>
    </div>
  );
}