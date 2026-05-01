
import type { ReceiveRequestItem } from "../model/ReceiveRequestItem";
type Props = {
  requests: ReceiveRequestItem[];
  selected: ReceiveRequestItem | null;
  loading: boolean;
  onSelect: (request: ReceiveRequestItem) => void;
};

function RequestsReceiveList({
  requests,
  selected,
  loading,
  onSelect,
}: Props) {
  return (
    <div className="card column" style={{ width: "320px", gap: "8px" }}>
      <h3>Approved Requests</h3>

      {loading && <p className="text-muted">Loading...</p>}

      {!loading && requests.length === 0 && (
        <p className="text-muted">目前沒有可接收的委託單</p>
      )}

      {!loading &&
        requests.map((request) => (
          <div
            key={request.id}
            className={`list-item ${
              selected?.id === request.id ? "active" : ""
            }`}
            onClick={() => onSelect(request)}
          >
            <div className="column" style={{ gap: "4px" }}>
              <strong>{request.title ?? `Request #${request.id}`}</strong>
              <span className="text-muted">
                {request.requesterName ?? "Unknown requester"}
              </span>
              <span className="tag approved">{request.status}</span>
            </div>
          </div>
        ))}
    </div>
  );
}

export default RequestsReceiveList;