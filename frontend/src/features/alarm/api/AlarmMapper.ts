// api/AlarmMapper.ts
import type { AlarmResponse, AlarmItem } from "../model/AlarmData";

/**
 * 將 API 回傳的原始資料 (Response) 轉換為 UI 顯示用的物件 (Item)
 */
export const mapToAlarmItem = (data: AlarmResponse[]): AlarmItem[] => {
  return data.map((item) => {
    return {
      id: item.id,
      equipmentId: item.equipment_id,
      // 這裡可以處理如果後端沒給名稱時的預設值
      equipmentName: item.equipment_name || `機台 #${item.equipment_id}`, 
      errorCode: item.error_code,
      // 將 DB 的 boolean 轉成 UI 易讀的 Status 字串
      status: item.is_resolved ? "RESOLVED" : "ACTIVE",
      // 時間格式化處理
      time: formatDateTime(item.created_at),
      handlerId: item.handler_id,
    };
  });
};

/**
 * 內部輔助函數：處理日期顯示
 */
const formatDateTime = (dateStr: string): string => {
  const date = new Date(dateStr);
  return date.toLocaleString("zh-TW", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  });
};