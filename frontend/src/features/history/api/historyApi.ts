import { CONFIG } from "../../../config/config";
import type { HistoryRequestGroupDTO } from "../model/HistoryData";

export async function fetchHistory(): Promise<HistoryRequestGroupDTO[]> {
  const res = await fetch(`${CONFIG.API_BASE}/api/history`);

  if (!res.ok) {
    throw new Error(`Request failed (${res.status})`);
  }

  return res.json();
}
