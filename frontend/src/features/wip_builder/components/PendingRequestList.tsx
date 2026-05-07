import type { PendingSamplesGroupedByRequestDTO } from "../model/WIPBuilderData";

type Props = {
  items: PendingSamplesGroupedByRequestDTO[];
  stagedRequestIds: number[];
  currentBatchRecipeId: number | null;
  filterRecipeId: number | null;
  filterRecipeName: string | null;
  onToggle: (item: PendingSamplesGroupedByRequestDTO) => void;
  onFilterByRecipe: (recipeId: number, recipeName: string) => void;
  onClearFilter: () => void;
};

function PendingRequestList({
  items,
  stagedRequestIds,
  currentBatchRecipeId,
  filterRecipeId,
  filterRecipeName,
  onToggle,
  onFilterByRecipe,
  onClearFilter,
}: Props) {
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

      {filterRecipeId !== null && (
        <div className="filter-indicator">
          <div className="filter-indicator-content">
            <span className="filter-label">Filtering by:</span>
            <span className="filter-value">Recipe: {filterRecipeName}</span>
            <span className="filter-count">({items.length} results)</span>
          </div>
          <button
            type="button"
            className="filter-clear-btn"
            onClick={onClearFilter}
            title="Clear filter"
          >
            ✕
          </button>
        </div>
      )}

      <div className="dispatch-list">
        {items.length === 0 ? (
          <div className="text-muted">No pending requests</div>
        ) : (
          items.map((item) => (
            <div
              key={item.requestId}
              className={`dispatch-card ${stagedRequestIds.includes(item.requestId) ? "selected" : ""}`}
              onClick={() => {
                if (item.nextRecipeId != null) {
                  onFilterByRecipe(item.nextRecipeId, item.nextRecipeName || "");
                }
              }}
              style={{ cursor: item.nextRecipeId != null ? "pointer" : "default" }}
              title={item.nextRecipeId != null ? "Click to filter by this recipe" : undefined}
            >
              <div className="dispatch-card-header">
                <span className="dispatch-card-title">{item.requestTitle}</span>

                <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                  <span className="dispatch-count">{item.pendingSampleCount}</span>
                  <button
                    type="button"
                    className="button secondary request-action-btn"
                    onClick={(e) => {
                      e.stopPropagation();
                      onToggle(item);
                    }}
                    disabled={
                      !stagedRequestIds.includes(item.requestId) &&
                      currentBatchRecipeId !== null &&
                      item.nextRecipeId !== currentBatchRecipeId
                    }
                  >
                    {stagedRequestIds.includes(item.requestId) ? "移除" : "加入"}
                  </button>
                </div>
              </div>

              <div className="dispatch-card-meta">Request ID: {item.requestId}</div>

              <div className="dispatch-badges">
                <span className={`badge ${getPriorityClass(item.priority)}`}>
                  {item.priority}
                </span>
              </div>

              <div style={{ fontWeight: 600, color: "#111827", marginTop: 6 }}>
                <strong>Recipe: </strong>
                <span style={{ fontWeight: 600 }}>{item.nextRecipeName || "-"}</span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default PendingRequestList;