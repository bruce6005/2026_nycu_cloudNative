
import { CONFIG } from "../../../config/config";
export const getRequests = async () => {
  const res = await fetch(CONFIG.API_BASE + "/requests");
  return res.json();
};

export const createRequest = async (data: {
  title: string;
  factoryUserId: number;
  priority: number;
  description: string;
}) => {
  const res = await fetch(CONFIG.API_BASE + "/requests", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  return res.json();
};

export const getRequestById = async (id: number) => {
  const res = await fetch(`${CONFIG.API_BASE}/requests/${id}`);
  return res.json();
};
