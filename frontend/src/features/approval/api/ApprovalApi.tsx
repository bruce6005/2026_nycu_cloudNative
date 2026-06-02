import { authFetch } from "../../utils/apiClient";
export const fetchPendingOrders = async (approverId: number) => {
  const res = await authFetch(`/approval/pending?approverId=${approverId}`);
  return res.json();
};

export const handleApproval = async (
  id: number,
  approverId: number,
  action: "APPROVE" | "REJECT",
  reason?: string
) => {
  await authFetch(`/approval/${id}`, {
    method: "POST",
    body: JSON.stringify({
      approverId,
      action,
      reason
    })
  });
};
