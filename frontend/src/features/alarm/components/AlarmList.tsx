// components/AlarmList.tsx
import React from "react";
import type { AlarmItem } from "../model/AlarmData";

type Props = {
  alarms: AlarmItem[];
  selected: AlarmItem | null;
  onSelect: (item: AlarmItem) => void;
};

function AlarmList({ alarms, selected, onSelect }: Props) {
  return (
    <div className="list-container">
      <div className="list-header">異常機台清單 ({alarms.length})</div>
      <div className="list-items">
        {alarms.map((item) => (
          <div
            key={item.id}
            className={`list-item ${selected?.id === item.id ? "active" : ""}`}
            onClick={() => onSelect(item)}
          >
            <div className="item-title">{item.equipmentName}</div>
            <div className="item-subtitle">
              <span className="error-code-badge">{item.errorCode}</span>
              <span className="item-time">{item.time}</span>
            </div>
          </div>
        ))}
        {alarms.length === 0 && <div className="empty-state">目前無異常機台</div>}
      </div>
    </div>
  );
}

export default AlarmList;