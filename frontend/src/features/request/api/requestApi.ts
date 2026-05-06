import { CONFIG } from "../../../config/config";

export const getRequest = async () => {
  const res = await fetch(CONFIG.API_BASE + "/api/request");
  return res.json();
};

export const createRequest = async (data: {
  title: string;
  factoryUserId: number;
  priority: number;
  description: string;
}) => {
  const res = await fetch(CONFIG.API_BASE + "/api/request", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  return res.json();
};

export const getRequestById = async (id: number) => {
  const res = await fetch(`${CONFIG.API_BASE}/api/request/${id}`);
  return res.json();
};
