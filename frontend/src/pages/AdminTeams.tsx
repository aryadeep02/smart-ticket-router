import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import Navbar from "../components/Navbar";
import { getAdminTeams } from "../services/adminService";

import type { SupportTeam } from "../services/adminService";

export default function AdminTeams() {
  const navigate = useNavigate();

  const [teams, setTeams] = useState<SupportTeam[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadTeams = async () => {
      try {
        const data = await getAdminTeams();
        setTeams(data);
      } catch {
        setError("Failed to load support teams");
      } finally {
        setLoading(false);
      }
    };

    loadTeams();
  }, []);

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
          <h1>Support Teams</h1>

          <p>
            View the support teams responsible for different ticket categories.
          </p>
        </div>

        {loading && <p>Loading teams...</p>}

        {error && (
          <div className="form-error">
            {error}
          </div>
        )}

        {!loading && !error && (
          <div className="admin-list">
            {teams.map((team) => (
              <div
                className="admin-card"
                key={team.id}
              >
                <div className="admin-card-main">
                  <h3>{team.name}</h3>

                  <p>
                    {team.description}
                  </p>
                </div>

                <div className="admin-card-meta">
                  <span className="role-badge">
                    Team #{team.id}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}