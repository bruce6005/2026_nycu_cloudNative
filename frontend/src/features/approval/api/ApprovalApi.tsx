import { CONFIG } from "../../../config/config";
export const fetchPendingOrders = async (approverId: number) => {
  const res = await fetch(`${CONFIG.API_BASE}/approval/pending?approverId=${approverId}`);
  return res.json();
};

export const handleApproval = async (
  id: number,
  approverId: number,
  action: "APPROVE" | "REJECT",
  reason?: string
) => {
  await fetch(`${CONFIG.API_BASE}/approval/${id}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      approverId,
      action,
      reason
    })
  });
};