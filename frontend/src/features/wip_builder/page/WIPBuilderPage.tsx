import { useEffect, useMemo, useState } from "react";

import { createWIPBatch, fetchEquipments, fetchPendingSamples } from "../api/wipBuilderApi";
import PendingRequestList from "../components/PendingRequestList";
import EquipmentList from "../components/EquipmentList";
import CreateBatchPanel from "../components/CreateBatchPanel";
import type {
  EquipmentWithRecipesDTO,
  PendingSampleDTO,
} from "../model/WIPBuilderData";
import type { WIPBatchDTO } from "../../wip_management/model/WipManagementData";
import { useSse } from "../../utils/useSse";
import "../styles/style.css";
import type { AuthUser } from "../../auth/model/AuthUser";
type Props = {
  user: AuthUser;
};
function WIPBuilderPage({ user }: Props) {
  const [pendingSamples, setPendingSamples] = useState<PendingSampleDTO[]>([]);
  const [stagedSamples, setStagedSamples] = useState<PendingSampleDTO[]>([]);
  const [equipments, setEquipments] = useState<EquipmentWithRecipesDTO[]>([]);

  const [selectedEquipmentId, setSelectedEquipmentId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [createdBatch, setCreatedBatch] = useState<WIPBatchDTO | null>(null);

  const [filterRecipeId, setFilterRecipeId] = useState<number | null>(null);
  const [filterRecipeName, setFilterRecipeName] = useState<string | null>(null);

  const [filterEquipmentId, setFilterEquipmentId] = useState<number | null>(null);
  const [filterEquipmentName, setFilterEquipmentName] = useState<string | null>(null);

  const stagedSampleIds = useMemo(
    () => stagedSamples.map((sample) => sample.sampleId),
    [stagedSamples]
  );

  const currentBatchRecipeId = useMemo(() => {
    if (stagedSamples.length === 0) {
      return null;
    }

    return stagedSamples[0].recipeId ?? null;
  }, [stagedSamples]);

  const stagedRecipeName = useMemo(() => {
    if (stagedSamples.length === 0) {
      return null;
    }

    return stagedSamples[0].recipeName ?? null;
  }, [stagedSamples]);

  const requiredCapacity = useMemo(() => stagedSamples.length, [stagedSamples]);

  const selectedEquipment = useMemo(
    () => equipments.find((item) => item.id === selectedEquipmentId) ?? null,
    [equipments, selectedEquipmentId]
  );

  const filteredEquipments = useMemo(() => {
    if (filterRecipeId === null) {
      return equipments;
    }

    return equipments.filter((equipment) =>
      equipment.recipes.some((recipe) => recipe.id === filterRecipeId)
    );
  }, [equipments, filterRecipeId]);

  const filteredPendingSamples = useMemo(() => {
    return pendingSamples.filter((sample) => {
      if (filterRecipeId !== null && sample.recipeId !== filterRecipeId) {
        return false;
      }

      if (filterEquipmentId !== null) {
        const equipment = equipments.find((item) => item.id === filterEquipmentId);

        if (!equipment) {
          return false;
        }

        if (!equipment.recipes.some((recipe) => recipe.id === sample.recipeId)) {
          return false;
        }
      }

      return true;
    });
  }, [pendingSamples, filterRecipeId, filterEquipmentId, equipments]);

  const loadData = async () => {
    try {
      setError("");

      const [pending, equipmentList] = await Promise.all([
        fetchPendingSamples(),
        fetchEquipments(),
      ]);

      setPendingSamples(pending);
      setEquipments(equipmentList);

      setStagedSamples((current) =>
        current.filter((sample) =>
          pending.some((item) => item.sampleId === sample.sampleId)
        )
      );

      if (
        equipmentList.length > 0 &&
        !equipmentList.some((item) => item.id === selectedEquipmentId)
      ) {
        setSelectedEquipmentId(equipmentList[0].id);
      }
    } catch {
      setError("Cannot load dispatch data from backend");
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  useSse("REQUEST_UPDATED", loadData);

  const handleToggleSample = (sample: PendingSampleDTO) => {
    const exists = stagedSamples.some((item) => item.sampleId === sample.sampleId);

    if (exists) {
      setStagedSamples((current) =>
        current.filter((item) => item.sampleId !== sample.sampleId)
      );
      setCreatedBatch(null);
      setError("");
      return;
    }

    if (currentBatchRecipeId !== null && sample.recipeId !== currentBatchRecipeId) {
      setError("同一個 WIP batch 只能加入相同 Recipe 的 Sample");
      return;
    }

    if (selectedEquipment && sample.recipeId !== null) {
      const supported = selectedEquipment.recipes.some(
        (recipe) => recipe.id === sample.recipeId
      );

      if (!supported) {
        setError("這台機器不支援該 Sample 的 Recipe");
        return;
      }
    }

    setError("");
    setStagedSamples((current) => [...current, sample]);
    setCreatedBatch(null);
  };

  const handleRemoveSample = (sampleId: number) => {
    setStagedSamples((current) =>
      current.filter((sample) => sample.sampleId !== sampleId)
    );
    setCreatedBatch(null);
    setError("");
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

    if (currentBatchRecipeId !== null) {
      const supported = item.recipes.some(
        (recipe) => recipe.id === currentBatchRecipeId
      );

      if (!supported) {
        setError("目前已加入的 Sample Recipe 不支援此機器，請先移除或改選機器");
      } else {
        setError("");
      }
    }

    setCreatedBatch(null);
  };

  const handleCreate = async () => {
    if (!selectedEquipment || stagedSamples.length === 0 || currentBatchRecipeId === null) {
      setCreatedBatch(null);
      setError("請先加入至少一筆 Sample 並選擇設備");
      return;
    }

    if (!selectedEquipment.recipes.some((recipe) => recipe.id === currentBatchRecipeId)) {
      setCreatedBatch(null);
      setError("目前設備不支援此批次的 Recipe");
      return;
    }

    if (requiredCapacity > selectedEquipment.maxCapacity) {
      setCreatedBatch(null);
      setError("需求容量超過設備容量，請移除部分 Sample");
      return;
    }

    try {
      setLoading(true);
      setError("");
      setCreatedBatch(null);

      const batch = await createWIPBatch({
        equipmentId: selectedEquipment.id,
        recipeId: currentBatchRecipeId,
        sampleIds: stagedSamples.map((sample) => sample.sampleId),
        operatorId: user.id,
      });

      setCreatedBatch(batch);
      setStagedSamples([]);
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
          items={filteredPendingSamples}
          stagedSampleIds={stagedSampleIds}
          currentBatchRecipeId={currentBatchRecipeId}
          filterRecipeId={filterRecipeId}
          filterRecipeName={filterRecipeName}
          onToggle={handleToggleSample}
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
            handleFilterByEquipment(item.id, item.name);
          }}
          filterEquipmentId={filterEquipmentId}
          filterEquipmentName={filterEquipmentName}
          onClearFilter={handleClearEquipmentFilter}
        />
      </div>

      <div className="dispatch-right">
        <CreateBatchPanel
          stagedSamples={stagedSamples}
          selectedEquipment={selectedEquipment}
          stagedRecipeName={stagedRecipeName}
          requiredCapacity={requiredCapacity}
          onRemoveSample={handleRemoveSample}
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