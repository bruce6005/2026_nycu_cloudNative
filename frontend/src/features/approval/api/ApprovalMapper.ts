import type { ApprovalResponse, ApprovalItem } from "../models/ApprovalData";

export const mapToApprovalItem = (
  data: ApprovalResponse[]
): ApprovalItem[] => {
  return data.map((item) => ({
    id: item.id,
    title: `Request #${item.id}`,
    priorityLabel: item.priority,
    description: item.description,
    status: item.status,
  }));
};