import type {
  EquipmentWithRecipesDTO,
  PendingSamplesGroupedByRequestDTO,
  RecipeDTO,
  WIPBatchDTO,
} from "../model/WIPBuilderData";

type Props = {
  selectedRequest: PendingSamplesGroupedByRequestDTO | null;
  selectedEquipment: EquipmentWithRecipesDTO | null;
  selectedRecipe: RecipeDTO | null;
  onSelectRecipe: (recipe: RecipeDTO) => void;
  onCreate: () => void;
  loading: boolean;
  error?: string;
  success?: WIPBatchDTO | null;
};

function CreateBatchPanel({
  selectedRequest,
  selectedEquipment,
  selectedRecipe,
  onSelectRecipe,
  onCreate,
  loading,
  error,
  success,
}: Props) {
  const canCreate = Boolean(selectedRequest && selectedEquipment && selectedRecipe && !loading);

  return (
    <div className="card column dispatch-panel">
      <div className="dispatch-title">Create WIP Batch</div>
      <div className="text-muted dispatch-subtitle">
        Confirm request, equipment, and recipe before sending to the floor
      </div>

      <div className="dispatch-summary">
        <div className="dispatch-summary-row mb-1 no-wrap">
          <span className="detail-label">Request id</span>
          <span className="detail-value">
            {selectedRequest ? selectedRequest.requestTitle : "Select a request"}
          </span>
        </div>
        <div className="dispatch-summary-row mb-1 no-wrap
        ">
          <span className="detail-label">Equipment</span>
          <span className="detail-value">
            {selectedEquipment ? selectedEquipment.name : "Select an equipment"}
          </span>
        </div>
        <div className="dispatch-summary-row">
          <span className="detail-label">Recipe</span>
          <span className="detail-value">
            {selectedRecipe ? `${selectedRecipe.name} v${selectedRecipe.version}` : "Select a recipe"}
          </span>
        </div>
        <div className="dispatch-summary-row  mb-1 no-wrap">
          <span className="detail-label">Required Capacity</span>
          <span className="detail-value">
            {selectedRequest?.unassignedSampleIds.length ?? 0}
          </span>
        </div>
      </div>

      <div>
        <div className="detail-label mb-1 no-wrap">Available recipes</div>
        <div className="recipe-grid">
          {selectedEquipment?.recipes.length ? (
            selectedEquipment.recipes.map((recipe) => (
              <button
                key={recipe.id}
                type="button"
                className={`recipe-pill ${selectedRecipe?.id === recipe.id ? "selected" : ""}`}
                onClick={() => onSelectRecipe(recipe)}
              >
                {recipe.name}
                <span className="recipe-version">v{recipe.version}</span>
              </button>
            ))
          ) : (
            <div className="text-muted">No recipes for the selected equipment</div>
          )}
        </div>
      </div>

      <div className="dispatch-footer">
        <button type="button" className="button primary" onClick={onCreate} disabled={!canCreate}>
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