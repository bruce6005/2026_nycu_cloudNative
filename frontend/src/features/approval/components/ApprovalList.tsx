import React from "react";
import "../styles/style.css";
import type { ApprovalItem } from "../models/ApprovalData";

type Props = {
  orders: ApprovalItem[];
  selected: ApprovalItem | null;
  onSelect: (order: ApprovalItem) => void;
};

function ApprovalList({ orders, onSelect, selected }: Props) {
  return (
    <div className="card column">
      <div className="order-main-title">Pending Orders</div>

      {!orders || orders.length === 0 ? (
        <div className="text-muted">No pending orders</div>
      ) : (
        orders.map((o) => (
          <div
            key={o.id}
            onClick={() => onSelect(o)}
            className={`order-card ${
              selected?.id === o.id ? "selected" : ""
            }`}
          >
            <div className="order-card-header">
              <span className="order-title">{o.title}</span>
              {o.priorityLabel === "HIGH" && (
                <span className="priority-dot" />
              )}
            </div>

            <div className="order-sub">
              {o.priorityLabel === "HIGH" ? "High Priority" : "Normal"}
            </div>
          </div>
        ))
      )}
    </div>
  );
}

export default ApprovalList;