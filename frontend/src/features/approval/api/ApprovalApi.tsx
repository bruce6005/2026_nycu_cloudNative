import { CONFIG } from "@/config/config";

export const fetchPendingOrders = async () => {
  const res = await fetch(`${CONFIG.API_BASE}/approval/pending`);
  return res.json();
};

export const approveOrder = async (id: number) => {
  await fetch(`${CONFIG.API_BASE}/approval/${id}/approve`, {
    method: "POST"
  });
};

export const rejectOrder = async (id: number, reason: string) => {
  await fetch(`${CONFIG.API_BASE}/approval/${id}/reject`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ reason })
  });
};