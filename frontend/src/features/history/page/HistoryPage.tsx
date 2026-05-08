import { useEffect, useState } from "react";

import { fetchHistory } from "../api/historyApi";
import type { HistoryRequestGroupDTO } from "../model/HistoryData";

function HistoryPage() {
  const [historyGroups, setHistoryGroups] = useState<HistoryRequestGroupDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadHistory = async () => {
      try {
        setLoading(true);
        setError("");
        setHistoryGroups(await fetchHistory());
      } catch {
        setError("Cannot load history data from backend");
      } finally {
        setLoading(false);
      }
    };

    loadHistory();
  }, []);

  return (
    <div className="column" style={{ gap: "16px" }}>
      <div>
        <h2>History</h2>
        <p className="text-muted" style={{ margin: 0 }}>
          Samples are grouped by request.
        </p>
      </div>

      {error && <div className="card text-muted">{error}</div>}
      {loading && <div className="card text-muted">Loading history...</div>}

      {!loading && historyGroups.length === 0 && !error && (
        <div className="card">
          <span className="text-muted">No history data available yet.</span>
        </div>
      )}

      {!loading &&
        historyGroups.map((group) => (
          <div key={group.requestId} className="card column" style={{ gap: "12px" }}>
            <div className="flex" style={{ justifyContent: "space-between", alignItems: "flex-start" }}>
              <div>
                <h3>{group.requestTitle || `Request #${group.requestId}`}</h3>
                <div className="text-muted">Request ID: {group.requestId}</div>
                <div className="text-muted">
                  {group.requestDescription?.trim() || "No description"}
                </div>
              </div>
              <div className="column" style={{ alignItems: "flex-end", gap: "8px" }}>
                <span className={`tag ${group.requestStatus.toLowerCase()}`}>
                  {group.requestStatus}
                </span>
                <span className="text-muted">Samples: {group.sampleCount}</span>
              </div>
            </div>

            <table className="table">
              <thead>
                <tr>
                  <th>Sample</th>
                  <th>Barcode</th>
                  <th>Status</th>
                  <th>Batch</th>
                  <th>Equipment</th>
                  <th>Recipe</th>
                </tr>
              </thead>
              <tbody>
                {group.samples.map((sample) => (
                  <tr key={sample.sampleId}>
                    <td>{sample.sampleId}</td>
                    <td>{sample.barcode}</td>
                    <td>{sample.status}</td>
                    <td>
                      {sample.batchId
                        ? `#${sample.batchId} ${sample.batchStatus ?? ""}`.trim()
                        : "-"}
                    </td>
                    <td>{sample.equipmentName ?? "-"}</td>
                    <td>{sample.recipeName ?? "-"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ))}
    </div>
  );
}

export default HistoryPage;
