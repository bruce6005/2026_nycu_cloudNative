import React from "react";
import "../styles/style.css";

type Order = {
  id: number;
  name: string;
  priority: string;
  description?: string;
};

type Props = {
  order: Order | null;
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

      {/* 🔹 Header */}
      <div className="detail-header">
        <div className="detail-title">{order.name}</div>

        {order.priority === "HIGH" && (
          <span className="priority-badge">HIGH</span>
        )}
      </div>

      <div className="detail-id">ID #{order.id}</div>

      {/* 🔹 Rows */}
      <div className="detail-row">
        <div className="detail-label">Priority</div>
        <div className="detail-value">
          {order.priority === "HIGH" ? "High Priority" : "Normal"}
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