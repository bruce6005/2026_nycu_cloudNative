import { CONFIG } from "../../../config/config";
import type { ReceiveRequestItem } from "../model/ReceiveRequestItem";

export async function fetchApprovedRequests(): Promise<ReceiveRequestItem[]> {
  const response = await fetch(`${CONFIG.API_BASE}/requests?status=approved`);

  if (!response.ok) {
    throw new Error("Failed to fetch approved requests");
  }

  return response.json();
}

export async function receiveRequest(
  requestId: number
): Promise<ReceiveRequestItem> {
  const response = await fetch(`${CONFIG.API_BASE}/requests/${requestId}/receive`, {
    method: "PATCH",
  });

  if (!response.ok) {
    throw new Error("Failed to receive request");
  }

  return response.json();
}