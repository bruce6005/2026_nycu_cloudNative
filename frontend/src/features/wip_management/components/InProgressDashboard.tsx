import React from "react";
import type { WIPBatchDTO } from "../model/WipManagementData";

interface Props {
  runningBatches: WIPBatchDTO[];
  finishedBatches: WIPBatchDTO[];
  selectedId: number | null;
  onSelect: (batch: WIPBatchDTO) => void;
}

const InProgressDashboard: React.FC<Props> = ({ 
  runningBatches, 
  finishedBatches, 
  selectedId, 
  onSelect 
}) => {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: "16px", height: "100%", overflow: "hidden" }}>
      {/* Running Section */}
      <div className="card column" style={{ flex: 1.5, overflow: "hidden" }}>
        <div className="wip-title" style={{ marginBottom: "12px", fontSize: "20px" }}>In Progress</div>
        <div className="wip-scroll-list">
          {runningBatches.map((batch) => (
            <button
              key={batch.id}
              className={`wip-card ${selectedId === batch.id ? "selected" : ""}`}
              style={{ borderLeft: "4px solid #3b82f6" }}
              onClick={() => onSelect(batch)}
            >
              <div className="wip-card-header">
                <span className="wip-id">Batch #{batch.id}</span>
                <span className="batch-tag tag-running">RUNNING</span>
              </div>
              <div style={{ marginTop: "8px" }}>
                <strong>{batch.equipmentName}</strong>
                <div style={{ fontSize: "12px", opacity: 0.7 }}>
                  Started: {new Date(batch.startTime!).toLocaleTimeString()}
                </div>
              </div>
            </button>
          ))}
          {runningBatches.length === 0 && (
            <div className="text-muted" style={{ textAlign: "center", padding: "20px" }}>No active batches</div>
          )}
        </div>
      </div>

      {/* Finished Section */}
      <div className="card column" style={{ flex: 1, overflow: "hidden" }}>
        <div className="wip-title" style={{ marginBottom: "12px", fontSize: "18px" }}>Finished Tasks</div>
        <div className="wip-scroll-list">
          {finishedBatches.map((batch) => (
            <button
              key={batch.id}
              className={`wip-card ${selectedId === batch.id ? "selected" : ""}`}
              onClick={() => onSelect(batch)}
            >
              <div className="wip-card-header">
                <span className="wip-id">Batch #{batch.id}</span>
                <span className="batch-tag tag-finished">FINISHED</span>
              </div>
              <div style={{ fontSize: "12px", marginTop: "4px" }}>
                Ended At: {new Date(batch.endTime!).toLocaleTimeString()}
              </div>
            </button>
          ))}
          {finishedBatches.length === 0 && (
            <div className="text-muted" style={{ textAlign: "center", padding: "20px" }}>No recent history</div>
          )}
        </div>
      </div>
    </div>
  );
};

export default InProgressDashboard;
