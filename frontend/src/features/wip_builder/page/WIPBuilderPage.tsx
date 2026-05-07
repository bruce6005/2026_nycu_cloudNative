import { useEffect, useMemo, useState } from "react";

import { createWIPBatch, fetchEquipments, fetchPendingSamples } from "../api/wipBuilderApi";
import PendingRequestList from "../components/PendingRequestList";
import EquipmentList from "../components/EquipmentList";
import CreateBatchPanel from "../components/CreateBatchPanel";
import type {
  EquipmentWithRecipesDTO,
  PendingSamplesGroupedByRequestDTO,
} from "../model/WIPBuilderData";
import type { WIPBatchDTO } from "../../wip_management/model/WipManagementData";
import "../styles/style.css";

type Props = {};

function WIPBuilderPage({}: Props) {
  const [pendingGroups, setPendingGroups] = useState<PendingSamplesGroupedByRequestDTO[]>([]);
  const [equipments, setEquipments] = useState<EquipmentWithRecipesDTO[]>([]);
  const [stagedRequestIds, setStagedRequestIds] = useState<number[]>([]);
  const [selectedEquipmentId, setSelectedEquipmentId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [createdBatch, setCreatedBatch] = useState<WIPBatchDTO | null>(null);
  const [filterRecipeId, setFilterRecipeId] = useState<number | null>(null);
  const [filterRecipeName, setFilterRecipeName] = useState<string | null>(null);
  const [filterEquipmentId, setFilterEquipmentId] = useState<number | null>(null);
  const [filterEquipmentName, setFilterEquipmentName] = useState<string | null>(null);

  const stagedRequests = useMemo(
    () => pendingGroups.filter((item) => stagedRequestIds.includes(item.requestId)),
    [pendingGroups, stagedRequestIds]
  );

  const stagedRecipeId = useMemo(() => {
    if (stagedRequests.length === 0) {
      return null;
    }
    return stagedRequests[0].nextRecipeId ?? null;
  }, [stagedRequests]);

  const stagedRecipeName = useMemo(() => {
    if (stagedRequests.length === 0) {
      return null;
    }
    return stagedRequests[0].nextRecipeName ?? null;
  }, [stagedRequests]);

  const requiredCapacity = useMemo(
    () => stagedRequests.reduce((sum, request) => sum + request.pendingSampleCount, 0),
    [stagedRequests]
  );

  const selectedEquipment = useMemo(
    () => equipments.find((item) => item.id === selectedEquipmentId) ?? null,
    [equipments, selectedEquipmentId]
  );

  const filteredEquipments = useMemo(() => {
    if (filterRecipeId === null) return equipments;
    return equipments.filter((eq) => eq.recipes.some((r) => r.id === filterRecipeId));
  }, [equipments, filterRecipeId]);

  const filteredPendingGroups = useMemo(() => {
    return pendingGroups.filter((item) => {
      if (filterRecipeId !== null && item.nextRecipeId !== filterRecipeId) return false;
      if (filterEquipmentId !== null) {
        const eq = equipments.find((e) => e.id === filterEquipmentId);
        if (!eq) return false;
        // only show requests whose recipe is supported by the filtered equipment
        if (!eq.recipes.some((r) => r.id === item.nextRecipeId)) return false;
      }
      return true;
    });
  }, [pendingGroups, filterRecipeId, filterEquipmentId, equipments]);

  const loadData = async () => {
    try {
      setError("");
      const [pending, equipmentList] = await Promise.all([
        fetchPendingSamples(),
        fetchEquipments(),
      ]);

      setPendingGroups(pending);
      setEquipments(equipmentList);

      setStagedRequestIds((current) =>
        current.filter((id) => pending.some((item) => item.requestId === id))
      );

      if (
        equipmentList.length > 0 &&
        !equipmentList.some((item) => item.id === selectedEquipmentId)
      ) {
        const firstEquipment = equipmentList[0];
        setSelectedEquipmentId(firstEquipment.id);
      }
    } catch {
      setError("Cannot load dispatch data from backend");
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleToggleRequest = (item: PendingSamplesGroupedByRequestDTO) => {
    if (stagedRequestIds.includes(item.requestId)) {
      setStagedRequestIds((current) => current.filter((id) => id !== item.requestId));
      setCreatedBatch(null);
      return;
    }

    if (stagedRecipeId !== null && item.nextRecipeId !== stagedRecipeId) {
      setError("同一個 WIP batch 只能加入相同 Recipe 的委託單");
      return;
    }

    if (selectedEquipment && item.nextRecipeId) {
      const supported = selectedEquipment.recipes.some((recipe) => recipe.id === item.nextRecipeId);
      if (!supported) {
        setError("這台機器不支援該委託單的 Recipe");
        return;
      }
    }

    setError("");
    setStagedRequestIds((current) => [...current, item.requestId]);
    setCreatedBatch(null);
  };

  const handleRemoveRequest = (requestId: number) => {
    setStagedRequestIds((current) => current.filter((id) => id !== requestId));
    setCreatedBatch(null);
  };

  const handleFilterByRecipe = (recipeId: number, recipeName: string) => {
    setFilterRecipeId(recipeId);
    setFilterRecipeName(recipeName);
  };

  const handleClearFilter = () => {
    setFilterRecipeId(null);
    setFilterRecipeName(null);
  };

  const handleFilterByEquipment = (equipmentId: number, equipmentName: string) => {
    setFilterEquipmentId(equipmentId);
    setFilterEquipmentName(equipmentName);
  };

  const handleClearEquipmentFilter = () => {
    setFilterEquipmentId(null);
    setFilterEquipmentName(null);
  };

  const handleSelectEquipment = (item: EquipmentWithRecipesDTO) => {
    setSelectedEquipmentId(item.id);

    if (stagedRecipeId !== null) {
      const supported = item.recipes.some((recipe) => recipe.id === stagedRecipeId);
      if (!supported) {
        setError("目前已加入的委託單 Recipe 不支援此機器，請先移除或改選機器");
      } else {
        setError("");
      }
    }

    setCreatedBatch(null);
  };

  const handleCreate = async () => {
    if (!selectedEquipment || stagedRequests.length === 0 || stagedRecipeId === null) {
      setCreatedBatch(null);
      setError("請先加入至少一筆委託單並選擇設備");
      return;
    }

    if (!selectedEquipment.recipes.some((recipe) => recipe.id === stagedRecipeId)) {
      setCreatedBatch(null);
      setError("目前設備不支援此批次的 Recipe");
      return;
    }

    if (requiredCapacity > selectedEquipment.maxCapacity) {
      setCreatedBatch(null);
      setError("需求容量超過設備容量，請移除部分委託單");
      return;
    }

    try {
      setLoading(true);
      setError("");
      setCreatedBatch(null);

      const sampleIds = stagedRequests.flatMap((request) => request.unassignedSampleIds);
      const batch = await createWIPBatch({
        equipmentId: selectedEquipment.id,
        recipeId: stagedRecipeId,
        sampleIds,
      });

      setCreatedBatch(batch);
      setStagedRequestIds([]);
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
          <div className="dispatch-title">WIP Builder</div>
        </div>

        <PendingRequestList
          items={filteredPendingGroups}
          stagedRequestIds={stagedRequestIds}
          currentBatchRecipeId={stagedRecipeId}
          filterRecipeId={filterRecipeId}
          filterRecipeName={filterRecipeName}
          onToggle={handleToggleRequest}
          onFilterByRecipe={handleFilterByRecipe}
          onClearFilter={handleClearFilter}
        />
      </div>

      <div className="dispatch-middle">
        <EquipmentList
          items={filteredEquipments}
          selectedEquipmentId={selectedEquipmentId}
          onSelect={(item) => {
            handleSelectEquipment(item);
            // clicking equipment also filters left list
            handleFilterByEquipment(item.id, item.name);
          }}
          filterEquipmentId={filterEquipmentId}
          filterEquipmentName={filterEquipmentName}
          onClearFilter={handleClearEquipmentFilter}
        />
      </div>

      <div className="dispatch-right">
        <CreateBatchPanel
          stagedRequests={stagedRequests}
          selectedEquipment={selectedEquipment}
          stagedRecipeName={stagedRecipeName}
          requiredCapacity={requiredCapacity}
          onRemoveRequest={handleRemoveRequest}
          onCreate={handleCreate}
          loading={loading}
          error={error}
          success={createdBatch}
        />
      </div>
    </div>
  );
}

export default WIPBuilderPage;