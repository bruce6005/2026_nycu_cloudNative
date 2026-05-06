// components/AlarmAction.tsx
import React, { useState, useEffect } from "react";
import type { AlarmItem } from "../model/AlarmData";

type Props = {
  alarm: AlarmItem | null;
  onResolve: (id: number, notes: string) => Promise<void>;
};

function AlarmAction({ alarm, onResolve }: Props) {
  const [notes, setNotes] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  // 當切換選取的告警時，重置筆記內容
  useEffect(() => {
    setNotes("");
  }, [alarm?.id]);

  if (!alarm) return null;

  const handleSubmit = async () => {
    if (!notes.trim()) {
      alert("請填寫維修筆記再提交！");
      return;
    }
    setIsSubmitting(true);
    await onResolve(alarm.id, notes);
    setIsSubmitting(false);
  };

  return (
    <div className="action-container">
      <h3>故障排除處理</h3>
      <div className="input-group">
        <label>維修筆記 (Resolution Notes)</label>
        <textarea
          rows={5}
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          placeholder="請輸入檢修過程或解決方法..."
        />
      </div>
      <button
        className="btn-primary"
        onClick={handleSubmit}
        disabled={isSubmitting}
      >
        {isSubmitting ? "處理中..." : "標記為已修復"}
      </button>
      <p className="hint-text">
        * 點擊修復後，機台狀態將回歸至正常。
      </p>
    </div>
  );
}

export default AlarmAction;