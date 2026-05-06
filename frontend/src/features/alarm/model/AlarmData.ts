// model/AlarmData.ts

export type AlarmResponse = {
  id: number;
  equipment_id: number;
  equipment_name: string; // 關聯 Equipment 表
  error_code: string;
  is_resolved: boolean;
  created_at: string;
  handler_id: number | null;
  resolution_notes?: string;
};

export type AlarmItem = {
  id: number;
  equipmentId: number;
  equipmentName: string;
  errorCode: string;
  status: 'ACTIVE' | 'RESOLVED';
  time: string;
  handlerId: number | null;
};