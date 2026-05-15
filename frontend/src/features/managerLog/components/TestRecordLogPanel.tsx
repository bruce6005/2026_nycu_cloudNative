import type { TestRecordLogDTO } from "../model/ManagerDashboardData";

type Props = {
  logs: TestRecordLogDTO[];
};

function formatDateTime(value?: string | null) {
  if (!value) {
    return "-";
  }

  return value.replace("T", " ").slice(0, 19);
}

function getStatusClass(status: string) {
  const normalized = status.toUpperCase();

  if (normalized === "QUEUED" || normalized === "ASSIGNED") {
    return "pending";
  }

  if (normalized === "COMPLETED" || normalized === "PASS") {
    return "approved";
  }

  if (normalized === "FAILED" || normalized === "ERROR") {
    return "rejected";
  }

  return "pending";
}

function TestRecordLogPanel({ logs }: Props) {
  return (
    <section className="dashboard-section">
      <div className="dashboard-section-header">
        <div>
          <h3>Lab Operation Logs</h3>
          <div className="text-muted">{logs.length} records</div>
        </div>
      </div>

      {logs.length === 0 ? (
        <div className="card text-muted">No operation logs found</div>
      ) : (
        <div className="card dashboard-table-card">
          <table className="table dashboard-log-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Batch</th>
                <th>Equipment</th>
                <th>Operator</th>
                <th>Status</th>
                <th>Start Time</th>
                <th>End Time</th>
                <th>Detail</th>
              </tr>
            </thead>

            <tbody>
              {logs.map((log) => (
                <tr key={log.id}>
                  <td>#{log.id}</td>
                  <td>Batch #{log.batchId}</td>
                  <td>{log.equipmentName}</td>
                  <td>
                    {log.operatorName} #{log.operatorId}
                  </td>
                  <td>
                    <span className={`tag ${getStatusClass(log.resultStatus)}`}>
                      {log.resultStatus}
                    </span>
                  </td>
                  <td>{formatDateTime(log.startTime)}</td>
                  <td>{formatDateTime(log.endTime)}</td>
                  <td className="dashboard-log-detail">
                    {log.resultData || "-"}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}

export default TestRecordLogPanel;