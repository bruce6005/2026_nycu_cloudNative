import { CONFIG } from "../../../config/config";

/**
 * 獲取所有未處理的告警紀錄
 */
export const fetchActiveAlarms = async () => {
  const res = await fetch(`${CONFIG.API_BASE}/equipment/alarms/active`);
  return res.json();
};

/**
 * 處理告警 (解除告警)
 * @param id 告警 ID
 * @param handlerId 處理人 ID (User 表中的 id)
 * @param notes 維修備註 (對應 DB 中的 resolution_notes)
 */
export const handleResolveAlarm = async (
  id: number,
  handlerId: number,
  notes: string
) => {
  await fetch(`${CONFIG.API_BASE}/equipment/alarms/${id}/resolve`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      handlerId,
      notes
    })
  });
};