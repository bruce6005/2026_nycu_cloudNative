import { useEffect, useState } from "react";
import { fetchManagerDashboard } from "../api/ManagerDashboardApi";
import type { ManagerDashboardDTO } from "../model/ManagerDashboardData";
import RequestStatsPanel from "../components/RequestStatsPanel";
import EquipmentUsagePanel from "../components/EquipmentUsagePanel";
import TestRecordLogPanel from "../components/TestRecordLogPanel";
import { useSse } from "../../utils/useSse";
import "../styles/style.css";

function ManagerDashboardPage() {
  const [dashboard, setDashboard] = useState<ManagerDashboardDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const loadDashboard = async () => {
    try {
      setLoading(true);
      setError("");

      const data = await fetchManagerDashboard();
      
      setDashboard(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Cannot load manager dashboard");
    } finally {
      setLoading(false);
    }
  };

   // polling 
    useEffect(() => {
        loadDashboard();

        const timer = window.setInterval(() => {
            loadDashboard();
        }, 1000);

        return () => window.clearInterval(timer);
    }, []);

  useSse("REQUEST_UPDATED", loadDashboard);

  return (
    <div className="manager-dashboard-page">
        <div className="dashboard-header">
        <div>
            <h2>Manager Dashboard</h2>
            <div className="text-muted">
            Request overview, equipment usage, and lab operation logs
            </div>
        </div>

        <button
            type="button"
            className="button secondary"
            onClick={loadDashboard}
            disabled={loading}
        >
            {loading ? "Loading..." : "Refresh"}
        </button>
        </div>

        {error && <div className="card dashboard-error">{error}</div>}

        <div className="dashboard-content">
        <div className="dashboard-top-grid">
            <div className="dashboard-grid-left">
            <RequestStatsPanel stats={dashboard?.requestStats ?? null} />
            </div>

            <div className="dashboard-grid-right">
            <EquipmentUsagePanel items={dashboard?.equipmentUsage ?? []} />
            </div>
        </div>

        <div className="dashboard-bottom">
            <TestRecordLogPanel logs={dashboard?.logs ?? []} />
        </div>
        </div>
    </div>
    );
}

export default ManagerDashboardPage;