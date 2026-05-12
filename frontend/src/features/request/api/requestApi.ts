import { CONFIG } from "../../../config/config";

export type RequestSampleDTO = {
  barcode: string;
  status: string;
  recipeId?: number | null;
  recipeName?: string | null;
  recipeParameters?: string | null;
};

export type RequestDetailDTO = {
  id: number;
  title: string;
  status: string;
  factoryUserId: number;
  approverId: number;
  priority: string;
  description: string;
  rejectReason?: string;
  samples: RequestSampleDTO[];
};

export type RequestListItemDTO = Pick<
  RequestDetailDTO,
  "id" | "title" | "status" | "priority" | "description"
>;

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
  description?: string;
  rejectReason?: string;
  samples?: SampleDTO[];
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

export async function archiveRequest(id: number): Promise<void> {
  const res = await fetch(`${CONFIG.API_BASE}/api/request/${id}/archive`, {
    method: "PATCH",
  });

  if (!res.ok) {
    throw new Error(`Archive request failed (${res.status})`);
  }
}

export const getRequestById = async (id: number) => {
  const res = await fetch(`${CONFIG.API_BASE}/api/request/${id}`);
  if (!res.ok) throw new Error("Fetch failed");
  return res.json();
};
