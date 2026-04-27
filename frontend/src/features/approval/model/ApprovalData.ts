export type ApprovalResponse = {
  id: number;
  factoryUserId: number;
  approverId: number;
  title: string;
  priority: number;
  status: "PENDING" | "APPROVED" | "REJECTED";
  description: string;
  createTime: string;
  endTime?: string;
  draftContent?: any;
};

export type ApprovalItem = {
  id: number;
  title: string;
  factoryUserId: number;
  approverId: number;
  priority: number;
  priorityLabel: "HIGH" | "MEDIUM" | "LOW";
  description: string;
  status: string;
  createTime: string;
};