import { useState, useEffect } from "react";
import { RequestForm } from "../components/RequestForm";
import { RequestList } from "../components/RequestList";
import { RequestDetail } from "../components/RequestDetail";
import { getRequests } from "../api/requestApi";
import type { AuthUser } from "../../auth/model/AuthUser";

type Props = {
  user: AuthUser;
};

function RequestsPage({ user }: Props) {
  const [requests, setRequests] = useState([]);
  const [error, setError] = useState("");
  const [selectedId, setSelectedId] = useState<number | null>(null);

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

  if (selectedId !== null) {
    return <RequestDetail id={selectedId} onBack={() => setSelectedId(null)} />;
  }

  return (
    <div className="content">
      {error && <p style={{ color: "red" }}>{error}</p>}

      <div className="card">
        <h2>建立委託單</h2>
        <RequestForm userId={user.id} onSuccess={loadRequests} />
      </div>

      <div className="card">
        <h2>委託單清單</h2>
        <RequestList requests={requests} onSelect={(id) => setSelectedId(id)} />
      </div>
    </div>
  );
}

export default RequestsPage;