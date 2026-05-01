import { useEffect, useState } from "react";
import "../../../styles/global.css";

import RequestsReceiveList from "../components/RequestsReceiveList";
import RequestsReceiveDetail from "../components/RequestsReceiveDetail";
import {fetchApprovedRequests,receiveRequest} from "../api/RequestsReceiveApi";
import type { ReceiveRequestItem } from "../model/ReceiveRequestItem";

function RequestsReceivePage() {
  const [requests, setRequests] = useState<ReceiveRequestItem[]>([]);
  const [selected, setSelected] = useState<ReceiveRequestItem | null>(null);
  const [loading, setLoading] = useState(false);
  const [receiving, setReceiving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    loadApprovedRequests();
  }, []);

  const loadApprovedRequests = async () => {
    try {
      setLoading(true);
      setError("");

      const data = await fetchApprovedRequests();

      setRequests(data);
      setSelected(data.length > 0 ? data[0] : null);
    } catch {
      setError("無法載入已核准的委託單");
    } finally {
      setLoading(false);
    }
  };

  const handleReceive = async () => {
    if (!selected) return;

    try {
      setReceiving(true);
      setError("");

      const received = await receiveRequest(selected.id);

      const nextRequests = requests.filter(
        (request) => request.id !== received.id
      );

      setRequests(nextRequests);
      setSelected(nextRequests.length > 0 ? nextRequests[0] : null);
    } catch {
      setError("接收失敗，請確認該委託單是否仍為 approved 狀態");
    } finally {
      setReceiving(false);
    }
  };

  return (
    <div className="column" style={{ gap: "16px" }}>
      <div>
        <h2>Receive Requests</h2>
      </div>

      {error && (
        <div className="card">
          <span className="text-muted">{error}</span>
        </div>
      )}

      <div className="flex">
        <RequestsReceiveList
          requests={requests}
          selected={selected}
          loading={loading}
          onSelect={setSelected}
        />

        <RequestsReceiveDetail
          request={selected}
          receiving={receiving}
          onReceive={handleReceive}
        />
      </div>
    </div>
  );
}

export default RequestsReceivePage;