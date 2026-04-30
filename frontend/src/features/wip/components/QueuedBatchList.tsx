import React from "react";
import type { WIPBatchDTO } from "../model/WipData";

interface Props {
  batches: WIPBatchDTO[];
  selectedId: number | null;
  onSelect: (batch: WIPBatchDTO) => void;
}

const QueuedBatchList: React.FC<Props> = ({ batches, selectedId, onSelect }) => {
  return (
    <div className="card column" style={{ flex: 1, overflow: "hidden" }}>
      <div className="wip-title" style={{ marginBottom: "8px" }}>Queued Batches</div>
      <div className="text-muted" style={{ marginBottom: "16px", fontSize: "14px" }}>
        Batches waiting for execution scan
      </div>
      
      <div className="wip-scroll-list">
        {batches.map((batch) => (
          <button
            key={batch.id}
            className={`wip-card ${selectedId === batch.id ? "selected" : ""}`}
            onClick={() => onSelect(batch)}
          >
            <div className="wip-card-header">
              <span className="wip-id">Batch #{batch.id}</span>
              <span className="batch-tag tag-queued">QUEUED</span>
            </div>
            <div style={{ marginTop: "8px" }}>
              <div style={{ fontWeight: 600, fontSize: "14px" }}>{batch.equipmentName}</div>
              <div style={{ fontSize: "13px", color: "#6b7280" }}>{batch.recipeName}</div>
            </div>
          </button>
        ))}
        {batches.length === 0 && (
          <div className="text-muted" style={{ textAlign: "center", padding: "40px" }}>
            No queued batches
          </div>
        )}
      </div>
    </div>
  );
};

export default QueuedBatchList;
