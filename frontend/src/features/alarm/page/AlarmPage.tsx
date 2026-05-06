import { useEffect, useState } from "react";
import AlarmList from "../components/AlarmList";
import AlarmDetail from "../components/AlarmDetail";
import AlarmAction from "../components/AlarmAction";

import { fetchActiveAlarms, handleResolveAlarm } from "../api/AlarmApi";
import { mapToAlarmItem } from "../api/AlarmMapper";

import type { AlarmResponse, AlarmItem } from "../model/AlarmData";
import type { AuthUser } from "../../auth/model/AuthUser";

type Props = {
  user: AuthUser;
};

function AlarmPage({ user }: Props) {
  const [alarms, setAlarms] = useState<AlarmItem[]>([]);
  const [selected, setSelected] = useState<AlarmItem | null>(null);

  const handlerId = user.id;

  const loadData = async () => {
    try {
      const data: AlarmResponse[] = await fetchActiveAlarms();
      const uiData = mapToAlarmItem(data);

      setAlarms(uiData);

      setSelected((prev) => {
        if (!prev && uiData.length > 0) {
          return uiData[0];
        }

        if (prev && !uiData.some((alarm) => alarm.id === prev.id)) {
          return uiData[0] ?? null;
        }

        return prev;
      });
    } catch (error) {
      console.error("Failed to fetch alarms:", error);
    }
  };

  useEffect(() => {
    loadData();

    const timer = setInterval(() => {
      loadData();
    }, 30000);

    return () => clearInterval(timer);
  }, [handlerId]);

  const onResolve = async (id: number, notes: string) => {
    await handleResolveAlarm(id, handlerId, notes);
    setSelected(null);
    await loadData();
  };

  return (
    <div className="flex">
      <div className="card column" style={{ width: "280px" }}>
        <h3>Active Alarms</h3>
        <AlarmList
          alarms={alarms}
          onSelect={setSelected}
          selected={selected}
        />
      </div>

      <div className="card column" style={{ flex: 1 }}>
        <h3>Alarm Detail</h3>
        <AlarmDetail alarm={selected} />
      </div>

      <div className="card column" style={{ width: "320px" }}>
        <h3>Action</h3>
        <AlarmAction alarm={selected} onResolve={onResolve} />
      </div>
    </div>
  );
}

export default AlarmPage;