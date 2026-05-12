import { useState, useEffect } from "react";
import { RequestForm } from "../components/RequestForm";
import { RequestList } from "../components/RequestList";
import { getRequest, type RequestListItemDTO } from "../api/requestApi";
import type { AuthUser } from "../../auth/model/AuthUser";
import { useSse } from "../../utils/useSse";

type Props = {
  user: AuthUser;
};

function RequestPage({ user }: Props) {
  const [requests, setRequests] = useState<any[]>([]); // 修正：給予明確型別並改回複數變數名（代表複數資料）
  const [error, setError] = useState("");
  
  const loadRequest = async () => {
  try {
      const data: RequestListItemDTO[] = await getRequest();

      const visibleRequests = data.filter((req) => {
        const status = req.status?.toUpperCase();
        return status !== "ARCHIVED" && status !== "DELETED";
      });

      setRequests(visibleRequests);
      setError("");
    } catch {
      setError("無法載入委託單資料");
    }
  };

  useEffect(() => {
    loadRequest();
  }, []);

  // 接收到後端的 REQUEST_UPDATED 信號時，自動重新載入資料
  useSse("REQUEST_UPDATED", loadRequest);

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
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <h2 style={{ margin: 0 }}>委託單清單</h2>
          <button 
            className="button secondary" 
            onClick={loadRequest}
            style={{ padding: '6px 16px', borderRadius: '8px' }}
          >
            🔄 重新整理
          </button>
        </div>
        <RequestList requests={requests} onArchived={loadRequest} />
      </div>
    </div>
  );
}

export default RequestPage;