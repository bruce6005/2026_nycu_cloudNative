import { authFetch } from "../../utils/apiClient";
import type { WIPBatchDTO } from "../../wip_management/model/WipManagementData";
import type {
  CreateWIPBatchRequest,
  EquipmentWithRecipesDTO,
  PendingSampleDTO,
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


export async function fetchPendingSamples(): Promise<PendingSampleDTO[]> {
  const res = await authFetch("/api/wip_builder/pending");
  const data = await res.json();

  console.log("pending samples response:", data);

  return data;
}
export async function fetchEquipments(): Promise<EquipmentWithRecipesDTO[]> {
  const res = await authFetch("/api/wip_builder/equipments");
  return res.json();
}

export async function createWIPBatch(
  payload: CreateWIPBatchRequest
): Promise<WIPBatchDTO> {
  const res = await authFetch("/api/wip_builder", {
    method: "POST",
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    throw new Error(await parseErrorMessage(res));
  }

  return res.json();
}
