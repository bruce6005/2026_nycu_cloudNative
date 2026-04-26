import React from "react";
import "../styles/style.css";
import type { ApprovalItem } from "../models/ApprovalData";

type Props = {
  order: ApprovalItem | null;
};

function ApprovalDetail({ order }: Props) {
  if (!order) {
    return (
      <div className="card detail-card">
        <h3 className="detail-title">Detail</h3>
        <p className="text-muted">Select an order</p>
      </div>
    );
  }

  return (
    <div className="card detail-card">

      {/* Header */}
      <div className="detail-header">
        {/* name → title */}
        <div className="detail-title">{order.title}</div>

        {order.priorityLabel === "HIGH" && (
          <span className="priority-badge">HIGH</span>
        )}
      </div>

      <div className="detail-id">ID #{order.id}</div>

      {/* Rows */}
      <div className="detail-row">
        <div className="detail-label">Priority</div>
        <div className="detail-value">
          {order.priorityLabel === "HIGH"
            ? "High Priority"
            : order.priorityLabel === "MEDIUM"
            ? "Medium Priority"
            : "Normal"}
        </div>
      </div>

      <div className="detail-row">
        <div className="detail-label">Description</div>
        <div className="detail-value detail-desc">
          {order.description || "No description"}
        </div>
      </div>

    </div>
  );
}

export default ApprovalDetail;