export type RequestStatus =
  | "draft"
  | "submitted"
  | "approved"
  | "received"
  | "rejected";

export type ReceiveRequestItem = {
  id: number;
  title?: string;
  requesterName?: string;
  sampleName?: string;
  machineName?: string | null;
  recipeName?: string | null;
  status: RequestStatus;
  createdAt?: string | null;
};