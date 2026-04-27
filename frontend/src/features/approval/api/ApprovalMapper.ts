import type { ApprovalResponse, ApprovalItem } from "../model/ApprovalData";

const mapPriorityLabel = (priority: number): "HIGH" | "MEDIUM" | "LOW" => {
  if (priority <= 2) return "HIGH";
  if (priority <= 4) return "MEDIUM";
  return "LOW";
};

export const mapToApprovalItem = (
  data: ApprovalResponse[]
): ApprovalItem[] => {
  return data.map((item) => ({
    id: item.id,
    title: item.title,
    factoryUserId: item.factoryUserId,
    approverId: item.approverId,
    priority: item.priority,
    priorityLabel: mapPriorityLabel(item.priority),
    description: item.description,
    status: item.status,
    createTime: item.createTime,
  }));
};