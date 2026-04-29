import type { PendingSamplesGroupedByRequestDTO } from "../model/DispatchData";

type Props = {
  items: PendingSamplesGroupedByRequestDTO[];
  selectedRequestId: number | null;
  onSelect: (item: PendingSamplesGroupedByRequestDTO) => void;
};

function PendingRequestList({ items, selectedRequestId, onSelect }: Props) {
  const getPriorityClass = (priority: string) => {
    const normalized = priority.toUpperCase();

    if (normalized === "URGENT") return "priority-urgent";
    if (normalized === "HIGH") return "priority-high";
    if (normalized === "NORMAL") return "priority-normal";
    return "priority-low";
  };

  return (
    <div className="card column dispatch-panel">
      <div className="dispatch-title">Pending Requests</div>

      <div className="dispatch-list">
        {items.length === 0 ? (
          <div className="text-muted">No pending requests</div>
        ) : (
          items.map((item) => (
            <button
              key={item.requestId}
              type="button"
              className={`dispatch-card ${
                selectedRequestId === item.requestId ? "selected" : ""
              }`}
              onClick={() => onSelect(item)}
            >
              <div className="dispatch-card-header">
                <span className="dispatch-card-title">{item.requestTitle}</span>
                <span className="dispatch-count">{item.pendingSampleCount}</span>
              </div>

              <div className="dispatch-card-meta">Request ID: {item.requestId}</div>
              <div className="dispatch-card-meta">
                Description: {item.requestDescription?.trim() || "-"}
              </div>

              <div className="dispatch-badges">
                <span className={`badge ${getPriorityClass(item.priority)}`}>
                  {item.priority}
                </span>
              </div>

              <div className="dispatch-card-meta">
                Samples: {item.unassignedSampleIds.join(", ") || "-"}
              </div>
            </button>
          ))
        )}
      </div>
    </div>
  );
}

export default PendingRequestList;