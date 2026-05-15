import type { EquipmentWithRecipesDTO } from "../model/WIPBuilderData";

type Props = {
  items: EquipmentWithRecipesDTO[];
  selectedEquipmentId: number | null;
  onSelect: (item: EquipmentWithRecipesDTO) => void;
  filterEquipmentId?: number | null;
  filterEquipmentName?: string | null;
  onClearFilter?: () => void;
};

function EquipmentList({ items, selectedEquipmentId, onSelect, filterEquipmentId, filterEquipmentName, onClearFilter }: Props) {
  const getStatusClass = (status: string | null | undefined) => {
    const normalized = (status ?? "UNKNOWN").toUpperCase();

    if (normalized === "IDLE" || normalized === "READY" || normalized === "STANDBY") {
      return "status-idle";
    }
    if (normalized === "BUSY" || normalized === "RUNNING") {
      return "status-busy";
    }
    if (normalized === "MAINTENANCE" || normalized === "CLEANING" || normalized === "CALIBRATING") {
      return "status-maintenance";
    }
    if (normalized === "ERROR" || normalized === "OFFLINE") {
      return "status-error";
    }
    return "status-unknown";
  };

  return (
    <div className="card column dispatch-panel">
      <div className="dispatch-title">Equipments & Recipes</div>
      <div className="text-muted dispatch-subtitle">
      {filterEquipmentId != null ? (
        <div className="filter-indicator" style={{ marginBottom: 8 }}>
          <div className="filter-indicator-content">
            <span className="filter-label">Filtering by:</span>
            <span className="filter-value">Equipment: {filterEquipmentName}</span>
          </div>
          <button type="button" className="filter-clear-btn" onClick={onClearFilter}>
            ✕
          </button>
        </div>
      ) : (
        <div />
      )}
      </div>

      <div className="dispatch-list">
        {items.length === 0 ? (
          <div className="text-muted">No equipments found</div>
        ) : (
          items.map((item) => (
            <button
              key={item.id}
              type="button"
              className={`dispatch-card ${
                selectedEquipmentId === item.id ? "selected" : ""
              }`}
              onClick={() => onSelect(item)}
            >
              <div className="dispatch-card-header">
                <span className="dispatch-card-title">{item.name}</span>
                <span className="dispatch-count">{item.maxCapacity}</span>
              </div>

              <div className={`equipment-status ${getStatusClass(item.currentStatus)}`}>
                {item.currentStatus || "UNKNOWN"}
              </div>
              <div className="dispatch-card-meta">
                Capacity: {item.maxCapacity} | Recipes: {item.recipes.length}
              </div>
            </button>
          ))
        )}
      </div>
    </div>
  );
}

export default EquipmentList;