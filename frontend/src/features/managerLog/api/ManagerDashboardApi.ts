import { CONFIG } from "../../../config/config";
import type {
  EquipmentUsageDTO,
  ManagerDashboardDTO,
  RequestStatsDTO,
  TestRecordLogDTO,
} from "../model/ManagerDashboardData";

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
    return `Request failed (${res.status})`;
  }

  return `Request failed (${res.status})`;
}

export async function fetchRequestStats(): Promise<RequestStatsDTO> {
  const res = await fetch(`${CONFIG.API_BASE}/api/manager_dashboard/request-stats`);
  console.log("request-stats:", res);
  if (!res.ok) {
    throw new Error(await parseErrorMessage(res));
  }

  return res.json();
}

export async function fetchEquipmentUsage(): Promise<EquipmentUsageDTO[]> {
  const res = await fetch(`${CONFIG.API_BASE}/api/manager_dashboard/equipment-usage`);
  console.log("equipment-usage:", res);
  if (!res.ok) {
    throw new Error(await parseErrorMessage(res));
  }

  return res.json();
}

export async function fetchTestRecordLogs(): Promise<TestRecordLogDTO[]> {
  const res = await fetch(`${CONFIG.API_BASE}/api/manager_dashboard/test-records`);
  console.log("test-records:", res);
  if (!res.ok) {
    throw new Error(await parseErrorMessage(res));
  }

  return res.json();
}

export async function fetchManagerDashboard(): Promise<ManagerDashboardDTO> {
  const [requestStats, equipmentUsage, logs] = await Promise.all([
    fetchRequestStats(),
    fetchEquipmentUsage(),
    fetchTestRecordLogs(),
  ]);

  return {
    requestStats,
    equipmentUsage,
    logs,
  };
}