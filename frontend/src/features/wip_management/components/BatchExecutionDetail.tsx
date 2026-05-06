import React from "react";
import type { WIPBatchDTO } from "../model/WipManagementData";

interface Props {
  batch: WIPBatchDTO | null;
  loading: boolean;
  onStart: (id: number) => void;
  onFinish: (id: number) => void;
}

const BatchExecutionDetail: React.FC<Props> = ({ batch, loading, onStart, onFinish }) => {
  if (!batch) {
    return (
      <div className="detail-panel" style={{ justifyContent: "center", alignItems: "center", color: "#9ca3af" }}>
        <p>Select a batch to see execution details</p>
      </div>
    );
  }

  return (
    <div className="detail-panel">
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
        <h2 style={{ margin: 0, fontSize: "20px" }}>Batch #{batch.id}</h2>
        <span className={`batch-tag tag-${batch.status.toLowerCase()}`}>{batch.status}</span>
      </div>

      <div className="detail-section">
        <h5>Process Context</h5>
        <div className="info-grid">
          <div className="info-item"><label>Equipment</label><span>{batch.equipmentName}</span></div>
          <div className="info-item"><label>Recipe</label><span>{batch.recipeName}</span></div>
        </div>
      </div>

      <div className="detail-section">
        <h5>Timestamps</h5>
        <div className="info-grid">
          <div className="info-item"><label>Created</label><span>{new Date(batch.createTime).toLocaleString()}</span></div>
          {batch.startTime && <div className="info-item"><label>Started</label><span>{new Date(batch.startTime).toLocaleString()}</span></div>}
          {batch.endTime && <div className="info-item"><label>Finished</label><span>{new Date(batch.endTime).toLocaleString()}</span></div>}
        </div>
      </div>

      <div className="detail-section" style={{ flex: 1 }}>
        <h5>Samples ({batch.sampleBarcodes.length})</h5>
        <div className="barcode-list">
          {batch.sampleBarcodes.map((code) => (
            <span key={code} className="barcode-pill">{code}</span>
          ))}
        </div>
      </div>

      <div className="detail-actions">
        {batch.status === "QUEUED" && (
          <button className="btn-action btn-start" onClick={() => onStart(batch.id)} disabled={loading}>
            {loading ? "Processing..." : "START SCANNING & RUN"}
          </button>
        )}
        {batch.status === "RUNNING" && (
          <button className="btn-action btn-finish" onClick={() => onFinish(batch.id)} disabled={loading}>
            {loading ? "Processing..." : "COMPLETE EXECUTION"}
          </button>
        )}
        {batch.status === "FINISHED" && (
          <div style={{ textAlign: "center", color: "#059669", fontWeight: 600, padding: "20px" }}>
            ✓ Task Completed
          </div>
        )}
      </div>
    </div>
  );
};

export default BatchExecutionDetail;
