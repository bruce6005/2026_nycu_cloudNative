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

function formatSeconds(seconds: number) {
  if (!seconds || seconds <= 0) {
    return "0s";
  }

  const minutes = Math.floor(seconds / 60);
  const remainSeconds = seconds % 60;

  if (minutes === 0) {
    return `${remainSeconds}s`;
  }

  return `${minutes}m ${remainSeconds}s`;
}

function EquipmentUsagePanel({ items }: Props) {
  const totalUsageCount =
    items.length === 0
      ? 0
      : Math.max(...items.map((item) => item.totalUsageCount ?? 0));

  const totalFinished = items.reduce(
    (sum, item) => sum + (item.successCount ?? 0),
    0
  );

  const totalFailed = items.reduce(
    (sum, item) => sum + (item.failedCount ?? 0),
    0
  );

  return (
    <section className="dashboard-section">
      <div className="dashboard-section-header">
        <div>
          <h3>Equipment Usage</h3>
          <div className="text-muted">
            Total usage {totalUsageCount} batches, finished {totalFinished},
            failed {totalFailed}
          </div>
        </div>
      </div>

      {items.length === 0 ? (
        <div className="card text-muted">No equipment usage data</div>
      ) : (
        <div className="dashboard-equipment-grid">
          {items.map((item) => {
            const usageRate = item.usageRate ?? 0;
            const usageClass = getUsageClass(usageRate);

            const usageCount = item.usageCount ?? 0;
            const itemTotalUsageCount = item.totalUsageCount ?? 0;
            const averageRunSeconds = item.averageRunSeconds ?? 0;
            const successCount = item.successCount ?? 0;
            const failedCount = item.failedCount ?? 0;
            const failureRate = item.failureRate ?? 0;
            const activeProgress = item.activeProgressPercent ?? 0;
            const remainingSeconds = item.remainingSeconds ?? 0;

            return (
              <div
                key={item.equipmentId}
                className="card dashboard-equipment-card"
              >
                <div className="dashboard-equipment-header">
                  <div>
                    <div className="dashboard-equipment-name">
                      {item.equipmentName}
                    </div>
                    <div className="text-muted">{item.equipmentType}</div>
                  </div>

                  <div className={`dashboard-usage-rate ${usageClass}`}>
                    {usageRate.toFixed(1)}%
                  </div>
                </div>

                <div className="dashboard-usage-bar-bg">
                  <div
                    className={`dashboard-usage-bar ${usageClass}`}
                    style={{ width: `${Math.min(usageRate, 100)}%` }}
                  />
                </div>

                <div className="dashboard-equipment-meta">
                  Usage Count: {usageCount} / {itemTotalUsageCount} batches
                </div>

                <div className="dashboard-equipment-meta">
                  Avg Run Time: {formatSeconds(averageRunSeconds)}
                </div>

                <div className="dashboard-equipment-meta">
                  Success: {successCount} | Failed: {failedCount} | Failure
                  Rate: {failureRate.toFixed(1)}%
                </div>

                <div className="dashboard-equipment-meta">
                  Status: {item.currentStatus ?? "UNKNOWN"}
                </div>

                {item.activeBatchId ? (
                  <div className="equipment-active-box">
                    <div className="dashboard-equipment-meta">
                      Active Batch: #{item.activeBatchId} |{" "}
                      {item.activeBatchStatus ?? "-"}
                    </div>

                    <div className="dashboard-usage-bar-bg active-progress-bg">
                      <div
                        className="dashboard-usage-bar usage-medium"
                        style={{ width: `${Math.min(activeProgress, 100)}%` }}
                      />
                    </div>

                    <div className="dashboard-equipment-meta">
                      Progress: {activeProgress}% | Remaining:{" "}
                      {remainingSeconds}s
                    </div>
                  </div>
                ) : (
                  <div className="dashboard-equipment-meta">
                    No active batch
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </section>
  );
}

export default EquipmentUsagePanel;