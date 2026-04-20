import React from "react";
import "../styles/style.css";

type Order = {
  id: number;
  name: string;
  priority: string;
};

type Props = {
  orders: Order[];
  onSelect: (order: Order) => void;
  selected: Order | null;
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
              <span className="order-title">{o.name}</span>
              {o.priority === "HIGH" && (
                <span className="priority-dot" />
              )}
            </div>

            <div className="order-sub">
              {o.priority === "HIGH" ? "High Priority" : "Normal"}
            </div>
          </div>
        ))
      )}
    </div>
  );
}

export default ApprovalList;