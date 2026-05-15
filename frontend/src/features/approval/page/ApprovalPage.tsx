import { useEffect, useState } from "react";
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
import { useSse } from "../../utils/useSse";

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

    // 只有在原本沒選中任何東西，或是原本選中的單子已經不見時，才自動選第一筆
    setSelected(prev => {
        if (!prev) return uiData[0] ?? null;
        // 檢查原本選中的那筆是否還在名單內 (有可能被處理掉了)
        const stillExists = uiData.find(o => o.id === prev.id);
        return stillExists ? stillExists : (uiData[0] ?? null);
    });
  };

  // 當後端有任何狀態變更時，立即刷新經理的待審核清單
  useSse("REQUEST_UPDATED", loadData);

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