import { useEffect, useMemo, useState } from "react";

import { createWIPBatch, fetchEquipments, fetchPendingSamples } from "../api/dispatchApi";
import PendingRequestList from "../components/PendingRequestList";
import EquipmentList from "../components/EquipmentList";
import CreateBatchPanel from "../components/CreateBatchPanel";
import type {
  EquipmentWithRecipesDTO,
  PendingSamplesGroupedByRequestDTO,
  RecipeDTO,
  WIPBatchDTO,
} from "../model/DispatchData";
import type { AuthUser } from "../../auth/model/AuthUser";
import "../styles/style.css";

type Props = {
  user: AuthUser;
};

function DispatchPage({ user }: Props) {
  const [pendingGroups, setPendingGroups] = useState<PendingSamplesGroupedByRequestDTO[]>([]);
  const [equipments, setEquipments] = useState<EquipmentWithRecipesDTO[]>([]);
  const [selectedRequestId, setSelectedRequestId] = useState<number | null>(null);
  const [selectedEquipmentId, setSelectedEquipmentId] = useState<number | null>(null);
  const [selectedRecipeId, setSelectedRecipeId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [createdBatch, setCreatedBatch] = useState<WIPBatchDTO | null>(null);

  const selectedRequest = useMemo(
    () => pendingGroups.find((item) => item.requestId === selectedRequestId) ?? null,
    [pendingGroups, selectedRequestId]
  );

  const selectedEquipment = useMemo(
    () => equipments.find((item) => item.id === selectedEquipmentId) ?? null,
    [equipments, selectedEquipmentId]
  );

  const selectedRecipe = useMemo(() => {
    if (!selectedEquipment || selectedRecipeId === null) {
      return null;
    }

    return selectedEquipment.recipes.find((item) => item.id === selectedRecipeId) ?? null;
  }, [selectedEquipment, selectedRecipeId]);

  const loadData = async () => {
    try {
      setError("");
      const [pending, equipmentList] = await Promise.all([
        fetchPendingSamples(),
        fetchEquipments(),
      ]);

      setPendingGroups(pending);
      setEquipments(equipmentList);

      if (pending.length > 0 && !pending.some((item) => item.requestId === selectedRequestId)) {
        setSelectedRequestId(pending[0].requestId);
      }

      if (
        equipmentList.length > 0 &&
        !equipmentList.some((item) => item.id === selectedEquipmentId)
      ) {
        const firstEquipment = equipmentList[0];
        setSelectedEquipmentId(firstEquipment.id);
        setSelectedRecipeId(firstEquipment.recipes[0]?.id ?? null);
      }
    } catch {
      setError("Cannot load dispatch data from backend");
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleSelectRequest = (item: PendingSamplesGroupedByRequestDTO) => {
    setSelectedRequestId(item.requestId);
    setCreatedBatch(null);
  };

  const handleSelectEquipment = (item: EquipmentWithRecipesDTO) => {
    setSelectedEquipmentId(item.id);
    setSelectedRecipeId(item.recipes[0]?.id ?? null);
    setCreatedBatch(null);
  };

  const handleSelectRecipe = (recipe: RecipeDTO) => {
    setSelectedRecipeId(recipe.id);
    setCreatedBatch(null);
  };

  const handleCreate = async () => {
    if (!selectedRequest || !selectedEquipment || !selectedRecipe) {
      setCreatedBatch(null);
      setError("Please select request, equipment, and recipe first");
      return;
    }

    try {
      setLoading(true);
      setError("");
      setCreatedBatch(null);
      const batch = await createWIPBatch({
        equipmentId: selectedEquipment.id,
        recipeId: selectedRecipe.id,
        sampleIds: selectedRequest.unassignedSampleIds,
      });

      setCreatedBatch(batch);
      await loadData();
    } catch (err) {
      setCreatedBatch(null);
      setError(err instanceof Error ? err.message : "Failed to create WIP batch");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="dispatch-layout">
      <div className="dispatch-left">
        <div className="card" style={{ marginBottom: "16px" }}>
          <div className="dispatch-title">Dispatch</div>
          <div className="text-muted dispatch-subtitle">Prepared for {user.name}</div>
        </div>

        <PendingRequestList
          items={pendingGroups}
          selectedRequestId={selectedRequestId}
          onSelect={handleSelectRequest}
        />
      </div>

      <div className="dispatch-middle">
        <EquipmentList
          items={equipments}
          selectedEquipmentId={selectedEquipmentId}
          onSelect={handleSelectEquipment}
        />
      </div>

      <div className="dispatch-right">
        <CreateBatchPanel
          selectedRequest={selectedRequest}
          selectedEquipment={selectedEquipment}
          selectedRecipe={selectedRecipe}
          onSelectRecipe={handleSelectRecipe}
          onCreate={handleCreate}
          loading={loading}
          error={error}
          success={createdBatch}
        />
      </div>
    </div>
  );
}

export default DispatchPage;