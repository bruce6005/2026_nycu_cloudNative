import { useState, useEffect } from "react";
import { RequestForm } from "../components/RequestForm";
import { RequestList } from "../components/RequestList";
import { RequestDetail } from "../components/RequestDetail";
import { getRequest } from "../api/requestApi";
import type { AuthUser } from "../../auth/model/AuthUser";

type Props = {
  user: AuthUser;
};

function RequestPage({ user }: Props) {
  const [requests, setRequests] = useState<any[]>([]); // 修正：給予明確型別並改回複數變數名（代表複數資料）
  const [error, setError] = useState("");
  const [selectedId, setSelectedId] = useState<number | null>(null);

  const loadRequest = async () => {
    try {
      const data = await getRequest();
      if (Array.isArray(data)) {
        setRequests(data);
      } else {
        console.error("Data is not an array:", data);
        setRequests([]);
        setError("資料格式錯誤");
      }
    } catch {
      setError("無法載入委託單資料");
    }
  };

  useEffect(() => {
    loadRequest();
  }, []);

  if (selectedId !== null) {
    return <RequestDetail id={selectedId} onBack={() => setSelectedId(null)} />;
  }

  return (
    <div className="flex" style={{ padding: '24px', alignItems: 'flex-start' }}>
      {error && <p style={{ color: "red" }}>{error}</p>}
      
      {/* 左側：表單操作區 */}
      <div className="column" style={{ flex: '0 0 320px' }}>
        <h2>建立委託單</h2>
        <RequestForm userId={user.id} onSuccess={loadRequest} />
      </div>

      {/* 右側：主清單區 */}
      <div className="column" style={{ flex: 1 }}>
        <h2>委託單清單</h2>
        <RequestList requests={requests} onSelect={(id) => setSelectedId(id)} />
      </div>
    </div>
  );
}

export default RequestPage;