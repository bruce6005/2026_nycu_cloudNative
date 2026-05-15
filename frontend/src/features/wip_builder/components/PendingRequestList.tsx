import type { PendingSampleDTO } from "../model/WIPBuilderData";

type Props = {
  items: PendingSampleDTO[];
  stagedSampleIds: number[];
  currentBatchRecipeId: number | null;
  filterRecipeId: number | null;
  filterRecipeName: string | null;
  onToggle: (item: PendingSampleDTO) => void;
  onFilterByRecipe: (recipeId: number, recipeName: string) => void;
  onClearFilter: () => void;
};

function PendingRequestList({
  items,
  stagedSampleIds = [],
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
      <div className="dispatch-title">Pending Samples</div>

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
            X
          </button>
        </div>
      )}

      <div className="dispatch-list">
        {items.length === 0 ? (
          <div className="text-muted">No pending samples</div>
        ) : (
          items.map((item) => {
            const isSelected = stagedSampleIds.includes(item.sampleId);
            const canAdd =
              isSelected ||
              currentBatchRecipeId === null ||
              item.recipeId === currentBatchRecipeId;

            return (
              <div
                key={item.sampleId}
                className={`dispatch-card ${isSelected ? "selected" : ""}`}
                onClick={() => {
                  if (item.recipeId != null) {
                    onFilterByRecipe(item.recipeId, item.recipeName || "");
                  }
                }}
                style={{ cursor: item.recipeId != null ? "pointer" : "default" }}
                title={item.recipeId != null ? "Click to filter by this recipe" : undefined}
              >
                <div className="dispatch-card-header">
                  <span className="dispatch-card-title">{item.barcode}</span>

                  <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                    <span className="dispatch-count">#{item.sampleId}</span>
                    <button
                      type="button"
                      className="button secondary request-action-btn"
                      onClick={(e) => {
                        e.stopPropagation();
                        onToggle(item);
                      }}
                      disabled={!canAdd}
                    >
                      {isSelected ? "移除" : "加入"}
                    </button>
                  </div>
                </div>

                <div className="dispatch-card-meta">
                  Request ID: {item.requestId} | {item.requestTitle}
                </div>

                <div className="dispatch-card-meta">
                  Sample Status: {item.sampleStatus}
                </div>

                <div className="dispatch-badges">
                  <span className={`badge ${getPriorityClass(item.priority)}`}>
                    {item.priority}
                  </span>
                </div>

                <div style={{ fontWeight: 600, color: "#111827", marginTop: 6 }}>
                  <strong>Recipe: </strong>
                  <span style={{ fontWeight: 600 }}>{item.recipeName || "-"}</span>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}

export default PendingRequestList;