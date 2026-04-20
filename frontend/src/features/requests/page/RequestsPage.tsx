import { useState, useEffect } from "react";
import { RequestForm } from "../components/RequestForm";
import { RequestList } from "../components/RequestList";
import { RequestDetail } from "../components/RequestDetail"; // 1. 引入詳情組件
import { getRequests } from "../api/requestApi";

function RequestsPage() {
  const [requests, setRequests] = useState([]);
  const [error, setError] = useState("");
  const [selectedId, setSelectedId] = useState<number | null>(null); // 2. 新增選中狀態

  const loadRequests = async () => {
    try {
      const data = await getRequests();
      setRequests(data);
    } catch {
      setError("無法載入委託單資料");
    }
  };

  useEffect(() => {
    loadRequests();
  }, []);

  // 3. 如果有選中 ID，切換顯示詳情
  if (selectedId !== null) {
    return <RequestDetail id={selectedId} onBack={() => setSelectedId(null)} />;
  }

  return (
    <div className="content">
      {error && <p style={{ color: 'red' }}>{error}</p>}

      <div className="card">
        <h2>建立委託單</h2>
        <RequestForm onSuccess={loadRequests} />
      </div>

      <div className="card">
        <h2>委託單清單</h2>
        <RequestList requests={requests} onSelect={(id) => setSelectedId(id)} />
      </div>
    </div>
  );
}

export default RequestsPage;
