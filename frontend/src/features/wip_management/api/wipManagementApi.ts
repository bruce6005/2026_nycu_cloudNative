import { CONFIG } from "../../../config/config";
import type { WIPBatchDTO } from "../../wip_management/model/WipManagementData";

async function parseErrorMessage(res: Response): Promise<string> {
  try {
    const data = await res.json();
    if (typeof data?.message === "string" && data.message.trim()) {
      return data.message;
    }
    if (typeof data?.error === "string" && data.error.trim()) {
      return data.error;
    }
  } catch {
    // Fallback
  }
  return `Request failed (${res.status})`;
}

export async function fetchWIPBatches(): Promise<WIPBatchDTO[]> {
  const res = await fetch(`${CONFIG.API_BASE}/api/wip_management`);
  return res.json();
}

export async function startWIPBatch(id: number): Promise<WIPBatchDTO> {
  const res = await fetch(`${CONFIG.API_BASE}/api/wip_management/${id}/start`, {
    method: "PATCH",
  });

  if (!res.ok) {
    throw new Error(await parseErrorMessage(res));
  }
  return res.json();
}

export async function finishWIPBatch(id: number): Promise<WIPBatchDTO> {
  const res = await fetch(`${CONFIG.API_BASE}/api/wip_management/${id}/finish`, {
    method: "PATCH",
  });

  if (!res.ok) {
    throw new Error(await parseErrorMessage(res));
  }
  return res.json();
}
