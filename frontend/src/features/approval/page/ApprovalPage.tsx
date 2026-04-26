import React, { useEffect, useState } from "react";
import ApprovalList from "../components/ApprovalList";
import ApprovalDetail from "../components/ApprovalDetail";
import ApprovalAction from "../components/ApprovalAction";
import "../styles/style.css";

// API
import {
  fetchPendingOrders,
  handleApproval,
} from "../api/ApprovalApi";

// 引入 mapper
import { mapToApprovalItem } from "../api/ApprovalMapper";

// 型別
import type { ApprovalResponse, ApprovalItem } from "../models/ApprovalData";

function ApprovalPage() {
  const [orders, setOrders] = useState<ApprovalItem[]>([]);
  const [selected, setSelected] = useState<ApprovalItem | null>(null);

  const approverId = 1;

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    const data: ApprovalResponse[] = await fetchPendingOrders(approverId);

    const uiData = mapToApprovalItem(data);

    setOrders(uiData);
    setSelected(uiData[0] ?? null);
  };

  const handleApprove = async (id: number) => {
    await handleApproval(id, approverId, "APPROVE");
    await loadData();
  };

  const handleReject = async (id: number, reason: string) => {
    await handleApproval(id, approverId, "REJECT", reason);
    await loadData();
  };

  return (
    <div className="approval-layout">

      <div className="approval-left">
        <ApprovalList
          orders={orders}
          onSelect={setSelected}
          selected={selected}
        />
      </div>

      <div className="approval-middle">
        <ApprovalDetail order={selected} />
      </div>

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