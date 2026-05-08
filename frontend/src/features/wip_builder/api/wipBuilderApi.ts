import { CONFIG } from "../../../config/config";
import type { WIPBatchDTO } from "../../wip_management/model/WipManagementData";
import type {
  CreateWIPBatchRequest,
  EquipmentWithRecipesDTO,
  PendingSamplesGroupedByRequestDTO,
} from "../model/WIPBuilderData";

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

export async function fetchPendingSamples(): Promise<PendingSamplesGroupedByRequestDTO[]> {
  const res = await fetch(`${CONFIG.API_BASE}/api/wip_builder/pending`);
  return res.json();
}

export async function fetchEquipments(): Promise<EquipmentWithRecipesDTO[]> {
  const res = await fetch(`${CONFIG.API_BASE}/api/wip_builder/equipments`);
  return res.json();
}

export async function createWIPBatch(
  payload: CreateWIPBatchRequest
): Promise<WIPBatchDTO> {
  const res = await fetch(`${CONFIG.API_BASE}/api/wip_builder`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    throw new Error(await parseErrorMessage(res));
  }

  return res.json();
}