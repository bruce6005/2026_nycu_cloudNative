import type { EquipmentUsageDTO } from "../model/ManagerDashboardData";

type Props = {
  items: EquipmentUsageDTO[];
};

function getUsageClass(usageRate: number) {
  if (usageRate >= 80) {
    return "usage-high";
  }

  if (usageRate >= 50) {
    return "usage-medium";
  }

  return "usage-low";
}

function EquipmentUsagePanel({ items }: Props) {
  const totalRunningMinutes = items.reduce(
    (sum, item) => sum + item.runningMinutes,
    0
  );

  const averageUsageRate =
    items.length === 0
      ? 0
      : items.reduce((sum, item) => sum + item.usageRate, 0) / items.length;

  return (
    <section className="dashboard-section">
      <div className="dashboard-section-header">
        <div>
          <h3>Equipment Usage</h3>
          <div className="text-muted">
            Average usage {averageUsageRate.toFixed(1)}%, total running{" "}
            {totalRunningMinutes} minutes
          </div>
        </div>
      </div>

      {items.length === 0 ? (
        <div className="card text-muted">No equipment usage data</div>
      ) : (
        <div className="dashboard-equipment-grid">
          {items.map((item) => {
            const usageClass = getUsageClass(item.usageRate);

            return (
              <div key={item.equipmentId} className="card dashboard-equipment-card">
                <div className="dashboard-equipment-header">
                  <div>
                    <div className="dashboard-equipment-name">
                      {item.equipmentName}
                    </div>
                    <div className="text-muted">{item.equipmentType}</div>
                  </div>

                  <div className={`dashboard-usage-rate ${usageClass}`}>
                    {item.usageRate.toFixed(1)}%
                  </div>
                </div>

                <div className="dashboard-usage-bar-bg">
                  <div
                    className={`dashboard-usage-bar ${usageClass}`}
                    style={{ width: `${Math.min(item.usageRate, 100)}%` }}
                  />
                </div>

                <div className="dashboard-equipment-meta">
                  Running: {item.runningMinutes} / {item.totalMinutes} min
                </div>

                <div className="dashboard-equipment-meta">
                  Status: {item.currentStatus ?? "UNKNOWN"}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </section>
  );
}

export default EquipmentUsagePanel;