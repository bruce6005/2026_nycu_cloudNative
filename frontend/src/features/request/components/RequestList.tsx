import React, { useState } from "react";
import { RequestDetail } from "./RequestDetail";
import type { RequestListItemDTO } from "../api/requestApi";
import { archiveRequest } from "../api/requestApi";

interface RequestListProps {
  requests: RequestListItemDTO[];
  onArchived?: () => void;
}

export const RequestList: React.FC<RequestListProps> = ({ requests, onArchived }) => {
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [archivingId, setArchivingId] = useState<number | null>(null);
  const [error, setError] = useState("");

  const toggleExpanded = (id: number) => {
    setExpandedId((current) => (current === id ? null : id));
  };

  const canArchive = (status: string) => {
    const normalized = status.toUpperCase();
    return normalized === "DONE" || normalized === "COMPLETED";
  };

  const handleArchive = async (id: number) => {
    const confirmed = window.confirm("確定要刪除這筆已完成委託單嗎？");

    if (!confirmed) {
      return;
    }

    try {
      setError("");
      setArchivingId(id);

      await archiveRequest(id);

      if (expandedId === id) {
        setExpandedId(null);
      }

      onArchived?.();
    } catch (err) {
      setError(err instanceof Error ? err.message : "刪除委託單失敗");
    } finally {
      setArchivingId(null);
    }
  };

  return (
    <div className="card request-list-panel">
      <h3 className="mb-1">委託單列表</h3>

      {error && <div className="request-list-error">{error}</div>}

      {requests.length === 0 ? (
        <p className="text-muted">目前沒有任何委託單</p>
      ) : (
        <div className="column request-list-scroll">
          {requests.map((req) => (
            <div
              key={req.id}
              className={`list-item column request-card ${
                expandedId === req.id ? "expanded" : ""
              }`}
              style={{
                borderBottom: "1px solid var(--border)",
                paddingBottom: "12px",
                marginBottom: "8px",
              }}
            >
              <div
                className="flex"
                style={{
                  justifyContent: "space-between",
                  alignItems: "center",
                  width: "100%",
                }}
              >
                <strong style={{ fontSize: "15px" }}>
                  #{req.id} - {req.title}
                </strong>

                <div className="flex" style={{ gap: "8px", alignItems: "center" }}>
                  <span className={`tag ${req.status.toLowerCase()}`}>
                    {req.status}
                  </span>

                  {canArchive(req.status) && (
                    <button
                      type="button"
                      className="button danger request-archive-button"
                      onClick={() => handleArchive(req.id)}
                      disabled={archivingId === req.id}
                    >
                      {archivingId === req.id ? "刪除中" : "刪除"}
                    </button>
                  )}

                  <button
                    type="button"
                    className="button secondary request-expand-button"
                    onClick={() => toggleExpanded(req.id)}
                  >
                    {expandedId === req.id ? "收合" : "展開"}
                  </button>
                </div>
              </div>

              <div
                className="text-muted mt-1"
                style={{
                  fontSize: "13px",
                  display: "flex",
                  alignItems: "center",
                }}
              >
                <span
                  className="tag"
                  style={{
                    marginRight: "8px",
                    background: req.priority === "URGENT" ? "#fee2e2" : "#f3f4f6",
                    color: req.priority === "URGENT" ? "#991b1b" : "#374151",
                    border:
                      req.priority === "URGENT"
                        ? "1px solid #fecaca"
                        : "1px solid #d1d5db",
                  }}
                >
                  {req.priority}
                </span>

                <span
                  style={{
                    flex: 1,
                    overflow: "hidden",
                    textOverflow: "ellipsis",
                    whiteSpace: "nowrap",
                  }}
                >
                  {req.description}
                </span>
              </div>

              {expandedId === req.id && (
                <div className="request-expanded-detail">
                  <RequestDetail id={req.id} inline />
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};