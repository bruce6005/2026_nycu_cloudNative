export type ApprovalResponse = {
  id: number;
  factoryUserId: number;
  approverId: number;
  priority: "HIGH" | "MEDIUM" | "LOW";
  status: "PENDING" | "APPROVED" | "REJECTED";
  description: string;
  createTime: string;
  endTime?: string;
  draftContent?: any;
};
export type ApprovalItem = {
  id: number;
  title: string;
  priorityLabel: string;
  description: string;
  status: string;
};