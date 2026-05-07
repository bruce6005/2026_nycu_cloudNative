import { CONFIG } from "../../../config/config";

export const getRequest = async () => {
  const res = await fetch(CONFIG.API_BASE + "/api/request");
  if (!res.ok) throw new Error("Fetch failed");
  return res.json();
};

export type SampleDTO = {
  barcode: string;
  recipeId: number;
};

export type RequestDTO = {
  title: string;
  factoryUserId: number;
  priority: "NORMAL" | "URGENT";
  description: string;
  samples: SampleDTO[];
};

export const createRequest = async (data: RequestDTO) => {
  const res = await fetch(CONFIG.API_BASE + "/api/request", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.message || "建立失敗");
  }
  
  return res.json();
};

export const getRequestById = async (id: number) => {
  const res = await fetch(`${CONFIG.API_BASE}/api/request/${id}`);
  if (!res.ok) throw new Error("Fetch failed");
  return res.json();
};
