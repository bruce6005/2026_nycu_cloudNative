import React, { useEffect, useState } from "react";
import ApprovalList from "../components/ApprovalList";
import ApprovalDetail from "../components/ApprovalDetail";
import ApprovalAction from "../components/ApprovalAction";
import "../styles/style.css";

import {
  fetchPendingOrders,
  handleApproval,
} from "../api/ApprovalApi";

import { mapToApprovalItem } from "../api/ApprovalMapper";

import type { ApprovalResponse, ApprovalItem } from "../model/ApprovalData";
import type { AuthUser } from "../../auth/model/AuthUser";

type Props = {
  user: AuthUser;
};

function ApprovalPage({ user }: Props) {
  const [orders, setOrders] = useState<ApprovalItem[]>([]);
  const [selected, setSelected] = useState<ApprovalItem | null>(null);

  const approverId = user.id;

  useEffect(() => {
    loadData();
  }, [approverId]);

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