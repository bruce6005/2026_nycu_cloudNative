import type { RequestStatsDTO } from "../model/ManagerDashboardData";
import StatCard from "./StatCard";

type Props = {
  stats: RequestStatsDTO | null;
};

function RequestStatsPanel({ stats }: Props) {
  return (
    <section className="dashboard-section">
      <h3>Request Statistics</h3>

      <div className="dashboard-stats-grid">
        <StatCard title="Total" value={stats?.totalRequests ?? 0} />
        <StatCard title="Pending" value={stats?.pendingRequests ?? 0} />
        <StatCard title="Approved" value={stats?.approvedRequests ?? 0} />
        <StatCard title="Dispatched" value={stats?.dispatchedRequests ?? 0} />
        <StatCard title="Completed" value={stats?.completedRequests ?? 0} />
        <StatCard title="Rejected" value={stats?.rejectedRequests ?? 0} />
      </div>
    </section>
  );
}

export default RequestStatsPanel;