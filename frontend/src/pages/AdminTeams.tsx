import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import Navbar from "../components/Navbar";
import { getAdminTeams } from "../services/adminService";

import type { SupportTeam } from "../services/adminService";

export default function AdminTeams() {
  const navigate = useNavigate();

  const [teams, setTeams] = useState<SupportTeam[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");

  useEffect(() => {
    const loadTeams = async () => {
      try {
        const data = await getAdminTeams();
        setTeams(data);
      } catch {
        setError("Unable to load support teams.");
      } finally {
        setLoading(false);
      }
    };

    loadTeams();
  }, []);

  const filteredTeams = useMemo(() => {
    const query = search.trim().toLowerCase();

    if (!query) {
      return teams;
    }

    return teams.filter(
      (team) =>
        team.name.toLowerCase().includes(query) ||
        team.description?.toLowerCase().includes(query)
    );
  }, [teams, search]);

  const getTeamInitials = (name: string) => {
    return name
      .split(/[\s_-]+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0])
      .join("")
      .toUpperCase();
  };

  return (
    <div>
      <Navbar />

      <main className="page-container admin-teams-page">
        <button
          className="secondary-button back-button"
          onClick={() => navigate("/dashboard")}
        >
          ← Back to Dashboard
        </button>

        {/* HERO */}
        <section className="admin-teams-hero">
          <div>
            <span className="admin-eyebrow">
              ADMINISTRATION
            </span>

            <h1>Support Teams</h1>

            <p>
              View the teams responsible for routing and resolving
              customer support requests.
            </p>
          </div>

          <div className="admin-users-count">
            <strong>{teams.length}</strong>
            <span>Active teams</span>
          </div>
        </section>

        {/* TEAM OVERVIEW */}
        {!loading && !error && (
          <section className="team-overview">
            <div className="team-overview-icon">⌁</div>

            <div>
              <strong>Intelligent ticket routing</strong>

              <p>
                Tickets are automatically routed to the appropriate
                support team based on their AI classification.
              </p>
            </div>
          </section>
        )}

        {/* DIRECTORY */}
        <section className="teams-directory">
          <div className="teams-directory-header">
            <div>
              <span className="admin-eyebrow">
                TEAM DIRECTORY
              </span>

              <h2>Available Support Teams</h2>

              <p>
                Browse the teams currently configured in the
                platform.
              </p>
            </div>

            <span className="admin-result-count">
              {filteredTeams.length}{" "}
              {filteredTeams.length === 1 ? "team" : "teams"}
            </span>
          </div>

          <div className="teams-search">
            <span>⌕</span>

            <input
              type="text"
              value={search}
              onChange={(event) =>
                setSearch(event.target.value)
              }
              placeholder="Search teams..."
            />
          </div>

          {loading && (
            <div className="teams-loading">
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
            filteredTeams.length === 0 && (
              <div className="admin-empty">
                <div className="admin-empty-icon">⌕</div>

                <h3>No teams found</h3>

                <p>
                  Try searching with a different team name.
                </p>
              </div>
            )}

          {!loading &&
            !error &&
            filteredTeams.length > 0 && (
              <div className="teams-grid">
                {filteredTeams.map((team) => (
                  <article
                    className="team-card"
                    key={team.id}
                  >
                    <div className="team-card-top">
                      <div className="team-avatar">
                        {getTeamInitials(team.name)}
                      </div>

                      <span className="team-id">
                        TEAM #{team.id}
                      </span>
                    </div>

                    <div className="team-card-content">
                      <h3>{team.name}</h3>

                      <p>
                        {team.description ||
                          "No description available."}
                      </p>
                    </div>

                    <div className="team-card-footer">
                      <span>
                        <i />
                        Routing enabled
                      </span>

                      <span className="team-arrow">
                        →
                      </span>
                    </div>
                  </article>
                ))}
              </div>
            )}
        </section>
      </main>
    </div>
  );
}