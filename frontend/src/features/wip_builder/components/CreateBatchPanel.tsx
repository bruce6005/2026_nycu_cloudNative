import type {
  EquipmentWithRecipesDTO,
  PendingSampleDTO,
} from "../model/WIPBuilderData";
import type { WIPBatchDTO } from "../../wip_management/model/WipManagementData";

type Props = {
  stagedSamples: PendingSampleDTO[];
  selectedEquipment: EquipmentWithRecipesDTO | null;
  stagedRecipeName: string | null;
  requiredCapacity: number;
  onRemoveSample: (sampleId: number) => void;
  onCreate: () => void;
  loading: boolean;
  error?: string;
  success?: WIPBatchDTO | null;
};

function CreateBatchPanel({
  stagedSamples,
  selectedEquipment,
  stagedRecipeName,
  requiredCapacity,
  onRemoveSample,
  onCreate,
  loading,
  error,
  success,
}: Props) {
  const canCreate = Boolean(
    stagedSamples.length > 0 &&
      selectedEquipment &&
      stagedRecipeName &&
      !loading
  );

  return (
    <div className="card column dispatch-panel create-batch-panel">
      <div className="dispatch-title">Create WIP Batch</div>
      <div className="text-muted dispatch-subtitle">
        Add samples from the left panel, then confirm the batch settings
      </div>

      <div className="dispatch-summary">
        <div className="dispatch-summary-row mb-1 no-wrap">
          <span className="detail-label">Equipment</span>
          <span className="detail-value">
            {selectedEquipment ? selectedEquipment.name : "Select an equipment"}
          </span>
        </div>

        <div className="dispatch-summary-row mb-1 no-wrap">
          <span className="detail-label">Equipment Type</span>
          <span className="detail-value">
            {selectedEquipment ? selectedEquipment.equipmentType : "-"}
          </span>
        </div>

        <div className="dispatch-summary-row mb-1 no-wrap">
          <span className="detail-label">Capacity</span>
          <span className="detail-value capacity-large">
            {requiredCapacity}/{selectedEquipment?.maxCapacity ?? 0}
          </span>
        </div>

        <div className="dispatch-summary-row mb-1 no-wrap">
          <span className="detail-label">Batch Recipe</span>
          <span className="detail-value">{stagedRecipeName ?? "-"}</span>
        </div>

        <div className="dispatch-summary-row flex-column">
          <span className="detail-label no-wrap">Available Recipes</span>
          <div className="recipes-container">
            {selectedEquipment && selectedEquipment.recipes.length > 0
              ? selectedEquipment.recipes.map((recipe, idx) => (
                  <span
                    key={recipe.id}
                    className={`recipe-tag recipe-tag-${idx % 5}`}
                  >
                    {recipe.name}
                  </span>
                ))
              : "-"}
          </div>
        </div>
      </div>

      <div className="batch-request-list-wrap">
        <div className="detail-label mb-1 no-wrap">Samples in this batch</div>

        <div className="batch-request-list">
          {stagedSamples.length === 0 ? (
            <div className="text-muted">No samples added yet</div>
          ) : (
            stagedSamples.map((sample) => (
              <div key={sample.sampleId} className="batch-request-item">
                <div>
                  <div className="batch-request-title">{sample.barcode}</div>

                  <div className="dispatch-card-meta">
                    Sample ID: {sample.sampleId} | Status: {sample.sampleStatus}
                  </div>

                  <div className="dispatch-card-meta">
                    Request: {sample.requestTitle} | Recipe:{" "}
                    {sample.recipeName || "-"}
                  </div>
                </div>

                <button
                  type="button"
                  className="button secondary batch-remove-btn"
                  onClick={() => onRemoveSample(sample.sampleId)}
                >
                  X
                </button>
              </div>
            ))
          )}
        </div>
      </div>

      <div className="dispatch-footer">
        <button
          type="button"
          className="button primary"
          onClick={onCreate}
          disabled={!canCreate}
        >
          {loading ? "Creating..." : "Create Batch"}
        </button>
      </div>

      {error && <div className="dispatch-error">{error}</div>}

      {success && (
        <div className="dispatch-success">
          Batch #{success.id} created at {success.createTime}
        </div>
      )}
    </div>
  );
}

export default CreateBatchPanel;