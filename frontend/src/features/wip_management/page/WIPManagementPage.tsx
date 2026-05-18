import React, { useCallback, useEffect, useState } from "react";
import { fetchWIPBatches, startWIPBatch, finishWIPBatch } from "../api/wipManagementApi";
import type { WIPBatchDTO } from "../model/WipManagementData";
import QueuedBatchList from "../components/QueuedBatchList";
import InProgressDashboard from "../components/InProgressDashboard";
import BatchExecutionDetail from "../components/BatchExecutionDetail";
import type { AuthUser } from "../../auth/model/AuthUser";
import { useSse } from "../../utils/useSse";
import "../styles/wip_management.css";

type Props = {
  user: AuthUser;
};

const WIPManagementPage: React.FC<Props> = ({ user }) => {
  const [batches, setBatches] = useState<WIPBatchDTO[]>([]);
  const [selectedBatch, setSelectedBatch] = useState<WIPBatchDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // useCallback 確保 useSse 拿到穩定的 reference，不會每次 render 重建連線
  const loadBatches = useCallback(async () => {
    try {
      setLoading(true);
      const data = await fetchWIPBatches();
      setBatches(data);
      // 用 functional updater 讀取最新 selectedBatch，避免 stale closure
      setSelectedBatch((prev) => {
        if (!prev) return prev;
        return data.find((b) => b.id === prev.id) ?? prev;
      });
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadBatches();
  }, [loadBatches]);

  // 收到後端 REQUEST_UPDATED 事件時自動重新載入（修復 WIP 階段狀態不即時問題）
  useSse("REQUEST_UPDATED", loadBatches);

  const handleStart = async (id: number) => {
    try {
      setLoading(true);
      await startWIPBatch(id);
      // 操作完成後，SSE 會自動觸發 loadBatches，但為了更好的體驗，手動觸發一次
      await loadBatches();
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleFinish = async (id: number) => {
    try {
      setLoading(true);
      await finishWIPBatch(id);
      // 同上
      await loadBatches();
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const queuedBatches = batches.filter(b => b.status === "QUEUED");
  const runningBatches = batches.filter(b => b.status === "RUNNING");
  const finishedBatches = batches.filter(b => b.status === "FINISHED");

  return (
    <div className="wip-layout">
      {/* Left Column: Queued */}
      <div className="wip-left">
        <div className="card" style={{ marginBottom: "16px" }}>
          <div className="wip-title">WIP Pipeline</div>
          <div className="text-muted wip-subtitle">Hello, {user.name}</div>
        </div>
        <QueuedBatchList 
          batches={queuedBatches} 
          selectedId={selectedBatch?.id || null} 
          onSelect={setSelectedBatch} 
        />
      </div>

      {/* Middle Column: Execution Dashboard */}
      <div className="wip-middle">
        <InProgressDashboard 
          runningBatches={runningBatches}
          finishedBatches={finishedBatches}
          selectedId={selectedBatch?.id || null}
          onSelect={setSelectedBatch}
        />
      </div>

      {/* Right Column: Detail & Actions */}
      <div className="wip-right">
        <BatchExecutionDetail 
          batch={selectedBatch}
          loading={loading}
          error={error}
          onStart={handleStart}
          onFinish={handleFinish}
        />
      </div>

    </div>
  );
};

export default WIPManagementPage;
