export type RequestStatsDTO = {
  totalRequests: number;
  pendingRequests: number;
  approvedRequests: number;
  dispatchedRequests: number;
  completedRequests: number;
  rejectedRequests: number;
};

export type EquipmentUsageDTO = {
  equipmentId: number;
  equipmentName: string;
  equipmentType: string;

  usageCount: number;
  totalUsageCount: number;
  usageRate: number;

  averageRunSeconds: number;

  successCount: number;
  failedCount: number;
  failureRate: number;

  currentStatus?: string | null;

  activeBatchId?: number | null;
  activeBatchStatus?: string | null;
  activeProgressPercent?: number | null;
  remainingSeconds?: number | null;
};

export type TestRecordLogDTO = {
  id: number;
  batchId: number;
  equipmentId: number;
  equipmentName: string;
  operatorId: number;
  operatorName: string;
  resultStatus: string;
  resultData?: string | null;
  startTime: string;
  endTime?: string | null;
};

export type ManagerDashboardDTO = {
  requestStats: RequestStatsDTO;
  equipmentUsage: EquipmentUsageDTO[];
  logs: TestRecordLogDTO[];
};