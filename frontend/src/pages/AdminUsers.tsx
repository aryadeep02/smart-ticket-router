import { useEffect, useMemo, useState } from "react";
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

type UserRole = "CUSTOMER" | "AGENT" | "ADMIN";

export default function AdminUsers() {
  const navigate = useNavigate();

  const [users, setUsers] = useState<AdminUser[]>([]);
  const [teams, setTeams] = useState<SupportTeam[]>([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState<UserRole>("CUSTOMER");
  const [supportTeamId, setSupportTeamId] = useState("");

  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState("");
  const [createSuccess, setCreateSuccess] = useState("");

  const [roleFilter, setRoleFilter] = useState("");
  const [teamFilter, setTeamFilter] = useState("");
  const [search, setSearch] = useState("");

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
        setError("Unable to load users and support teams.");
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  const handleCreateUser = async () => {
    setCreateError("");
    setCreateSuccess("");

    const trimmedName = name.trim();
    const trimmedEmail = email.trim();

    if (!trimmedName || !trimmedEmail || !password) {
      setCreateError("Name, email and password are required.");
      return;
    }

    if (password.length < 8) {
      setCreateError("Password must be at least 8 characters.");
      return;
    }

    if (role === "AGENT" && !supportTeamId) {
      setCreateError("Support team is required for an agent.");
      return;
    }

    setCreating(true);

    try {
      await createAdminUser({
        name: trimmedName,
        email: trimmedEmail,
        password,
        role,
        supportTeamId:
          role === "AGENT" ? Number(supportTeamId) : undefined,
      });

      setCreateSuccess("User created successfully.");

      setName("");
      setEmail("");
      setPassword("");
      setRole("CUSTOMER");
      setSupportTeamId("");

      const updatedUsers = await getAdminUsers();
      setUsers(updatedUsers);
    } catch (error: any) {
      setCreateError(
        error.response?.data?.message || "Failed to create user."
      );
    } finally {
      setCreating(false);
    }
  };

  const filteredUsers = useMemo(() => {
    const query = search.trim().toLowerCase();

    return users.filter((user) => {
      const matchesRole =
        !roleFilter || user.role === roleFilter;

      const matchesTeam =
        !teamFilter ||
        user.supportTeamId?.toString() === teamFilter;

      const matchesSearch =
        !query ||
        user.name.toLowerCase().includes(query) ||
        user.email.toLowerCase().includes(query);

      return matchesRole && matchesTeam && matchesSearch;
    });
  }, [users, roleFilter, teamFilter, search]);

  const customerCount = users.filter(
    (user) => user.role === "CUSTOMER"
  ).length;

  const agentCount = users.filter(
    (user) => user.role === "AGENT"
  ).length;

  const adminCount = users.filter(
    (user) => user.role === "ADMIN"
  ).length;

  const getInitials = (name: string) => {
    return name
      .split(" ")
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0])
      .join("")
      .toUpperCase();
  };

  /* =========================================================
     ROLE-BASED CREATE USER CONTENT
  ========================================================= */

  const getRoleLabel = () => {
    if (role === "AGENT") return "Agent";
    if (role === "ADMIN") return "Administrator";
    return "Customer";
  };

  const getCreateTitle = () => {
    return `Create ${getRoleLabel()}`;
  };

  const getCreateDescription = () => {
    if (role === "AGENT") {
      return "Add a support agent and assign them to a support team.";
    }

    if (role === "ADMIN") {
      return "Add an administrator with access to platform management.";
    }

    return "Add an authenticated customer who can create support tickets.";
  };

  const getCreateHelperText = () => {
    if (role === "AGENT") {
      return "Agents must belong to a support team.";
    }

    if (role === "ADMIN") {
      return "Administrators can manage users, teams and tickets.";
    }

    return "Customers can create and track their support tickets.";
  };

  return (
    <div>
      <Navbar />

      <main className="page-container admin-users-page">
        <button
          className="secondary-button back-button"
          onClick={() => navigate("/dashboard")}
        >
          ← Back to Dashboard
        </button>

        {/* HEADER */}
        <section className="admin-users-hero">
          <div>
            <span className="admin-eyebrow">
              ADMINISTRATION
            </span>

            <h1>User Management</h1>

            <p>
              Manage platform access, roles and support team
              assignments from one place.
            </p>
          </div>

          <div className="admin-users-count">
            <strong>{users.length}</strong>
            <span>Total users</span>
          </div>
        </section>

        {/* SUMMARY */}
        <section className="admin-user-stats">
          <div className="admin-stat-card">
            <span className="admin-stat-icon">👥</span>

            <div>
              <strong>{users.length}</strong>
              <span>Total Users</span>
            </div>
          </div>

          <div className="admin-stat-card">
            <span className="admin-stat-icon">◉</span>

            <div>
              <strong>{agentCount}</strong>
              <span>Agents</span>
            </div>
          </div>

          <div className="admin-stat-card">
            <span className="admin-stat-icon">◇</span>

            <div>
              <strong>{customerCount}</strong>
              <span>Customers</span>
            </div>
          </div>

          <div className="admin-stat-card">
            <span className="admin-stat-icon">◆</span>

            <div>
              <strong>{adminCount}</strong>
              <span>Admins</span>
            </div>
          </div>
        </section>

        {/* CREATE USER */}
        <section className="admin-create-panel">
          <div className="admin-section-heading">
            <div>
              <span className="admin-eyebrow">
                ACCESS CONTROL
              </span>

              <h2>{getCreateTitle()}</h2>

              <p>{getCreateDescription()}</p>
            </div>
          </div>

          <div className="admin-form-grid">
            <div className="admin-form-field">
              <label htmlFor="admin-name">
                Full name
              </label>

              <input
                id="admin-name"
                type="text"
                value={name}
                onChange={(event) =>
                  setName(event.target.value)
                }
                placeholder="e.g. Rahul Sharma"
              />
            </div>

            <div className="admin-form-field">
              <label htmlFor="admin-email">
                Email address
              </label>

              <input
                id="admin-email"
                type="email"
                value={email}
                onChange={(event) =>
                  setEmail(event.target.value)
                }
                placeholder="user@example.com"
              />
            </div>

            <div className="admin-form-field">
              <label htmlFor="admin-password">
                Password
              </label>

              <input
                id="admin-password"
                type="password"
                value={password}
                onChange={(event) =>
                  setPassword(event.target.value)
                }
                placeholder="Minimum 8 characters"
              />
            </div>

            <div className="admin-form-field">
              <label htmlFor="admin-role">
                Role
              </label>

              <select
                id="admin-role"
                value={role}
                onChange={(event) => {
                  const nextRole =
                    event.target.value as UserRole;

                  setRole(nextRole);

                  if (nextRole !== "AGENT") {
                    setSupportTeamId("");
                  }

                  setCreateError("");
                  setCreateSuccess("");
                }}
              >
                <option value="CUSTOMER">
                  Customer
                </option>

                <option value="AGENT">
                  Agent
                </option>

                <option value="ADMIN">
                  Administrator
                </option>
              </select>
            </div>

            {role === "AGENT" && (
              <div className="admin-form-field">
                <label htmlFor="admin-team">
                  Support team
                </label>

                <select
                  id="admin-team"
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
          </div>

          {createError && (
            <div className="admin-alert admin-alert-error">
              {createError}
            </div>
          )}

          {createSuccess && (
            <div className="admin-alert admin-alert-success">
              {createSuccess}
            </div>
          )}

          <div className="admin-create-footer">
            <span>
              {getCreateHelperText()}
            </span>

            <button
              className="primary-button"
              onClick={handleCreateUser}
              disabled={creating}
            >
              {creating
                ? "Creating..."
                : `+ Create ${getRoleLabel()}`}
            </button>
          </div>
        </section>

        {/* USER DIRECTORY */}
        <section className="admin-directory">
          <div className="admin-directory-header">
            <div>
              <span className="admin-eyebrow">
                DIRECTORY
              </span>

              <h2>Existing Users</h2>

              <p>
                Search and filter everyone currently
                registered on the platform.
              </p>
            </div>

            <span className="admin-result-count">
              {filteredUsers.length}{" "}
              {filteredUsers.length === 1
                ? "user"
                : "users"}
            </span>
          </div>

          <div className="admin-toolbar">
            <div className="admin-search">
              <span>⌕</span>

              <input
                type="text"
                value={search}
                onChange={(event) =>
                  setSearch(event.target.value)
                }
                placeholder="Search by name or email..."
              />
            </div>

            <select
              value={roleFilter}
              onChange={(event) =>
                setRoleFilter(event.target.value)
              }
            >
              <option value="">All roles</option>

              <option value="CUSTOMER">
                Customers
              </option>

              <option value="AGENT">
                Agents
              </option>

              <option value="ADMIN">
                Administrators
              </option>
            </select>

            <select
              value={teamFilter}
              onChange={(event) =>
                setTeamFilter(event.target.value)
              }
            >
              <option value="">All teams</option>

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
            <div className="admin-loading">
              <div />
              <div />
              <div />
            </div>
          )}

          {error && (
            <div className="admin-alert admin-alert-error">
              {error}
            </div>
          )}

          {!loading &&
            !error &&
            filteredUsers.length === 0 && (
              <div className="admin-empty">
                <div className="admin-empty-icon">
                  ⌕
                </div>

                <h3>No users found</h3>

                <p>
                  Try changing your search or filter
                  criteria.
                </p>
              </div>
            )}

          {!loading &&
            !error &&
            filteredUsers.length > 0 && (
              <div className="admin-user-table">
                <div className="admin-table-head">
                  <span>User</span>
                  <span>Role</span>
                  <span>Support Team</span>
                </div>

                {filteredUsers.map((user) => (
                  <div
                    className="admin-user-row"
                    key={user.id}
                  >
                    <div className="admin-user-identity">
                      <div className="admin-avatar">
                        {getInitials(user.name)}
                      </div>

                      <div>
                        <strong>{user.name}</strong>
                        <span>{user.email}</span>
                      </div>
                    </div>

                    <div>
                      <span
                        className={`admin-role-badge admin-role-${user.role.toLowerCase()}`}
                      >
                        {user.role}
                      </span>
                    </div>

                    <div className="admin-team-cell">
                      {user.supportTeamName ? (
                        <>
                          <span className="team-dot" />
                          {user.supportTeamName}
                        </>
                      ) : (
                        <span className="no-team">
                          No team assigned
                        </span>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
        </section>
      </main>
    </div>
  );
}