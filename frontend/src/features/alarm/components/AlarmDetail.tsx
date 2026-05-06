// components/AlarmDetail.tsx
import React from "react";
import type { AlarmItem } from "../model/AlarmData";

type Props = {
  alarm: AlarmItem | null;
};

function AlarmDetail({ alarm }: Props) {
  if (!alarm) {
    return <div className="detail-placeholder">請由左側選擇一個告警項目</div>;
  }

  return (
    <div className="detail-container">
      <h2>告警詳細資訊</h2>
      <hr />
      <div className="detail-row">
        <label>機台名稱：</label>
        <span>{alarm.equipmentName}</span>
      </div>
      <div className="detail-row">
        <label>機台編號 (ID)：</label>
        <span>{alarm.equipmentId}</span>
      </div>
      <div className="detail-row">
        <label>錯誤代碼：</label>
        <span className="text-danger">{alarm.errorCode}</span>
      </div>
      <div className="detail-row">
        <label>發生時間：</label>
        <span>{alarm.time}</span>
      </div>
      <div className="detail-row">
        <label>目前狀態：</label>
        <span className={`status-tag ${alarm.status.toLowerCase()}`}>
          {alarm.status === "ACTIVE" ? "處理中" : "已解除"}
        </span>
      </div>
    </div>
  );
}

export default AlarmDetail;