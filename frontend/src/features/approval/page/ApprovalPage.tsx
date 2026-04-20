import React, { useEffect, useState } from "react";
import ApprovalList from "../components/ApprovalList";
import ApprovalDetail from "../components/ApprovalDetail";
import ApprovalAction from "../components/ApprovalAction";
import "../styles/style.css";

type Order = {
  id: number;
  name: string;
  priority: string;
  description?: string;
};

function ApprovalPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [selected, setSelected] = useState<Order | null>(null);

  useEffect(() => {
    const mockData: Order[] = [
      {
        id: 1,
        name: "Order A",
        priority: "HIGH",
        description: "Urgent wafer test",
      },
      {
        id: 2,
        name: "Order B",
        priority: "LOW",
        description: "Normal process",
      },
    ];

    setOrders(mockData);
    setSelected(mockData[0]); // 👉 預設選第一筆（UX更好）
  }, []);

  const handleApprove = async (id: number) => {
    console.log("approve", id);

    setOrders((prev) => prev.filter((o) => o.id !== id));
    setSelected(null);
  };

  const handleReject = async (id: number, reason: string) => {
    console.log("reject", id, reason);

    setOrders((prev) => prev.filter((o) => o.id !== id));
    setSelected(null);
  };

  return (
    <div className="approval-layout">
      
      {/* 左 */}
      <div className="approval-left">
        <ApprovalList
          orders={orders}
          onSelect={setSelected}
          selected={selected}
        />
      </div>

      {/* 中 */}
      <div className="approval-middle">
        <ApprovalDetail order={selected} />
      </div>

      {/* 右 */}
      <div className="approval-right">
        <ApprovalAction
          order={selected}
          onApprove={handleApprove}
          onReject={handleReject}
        />
      </div>

    </div>
  );
}

export default ApprovalPage;