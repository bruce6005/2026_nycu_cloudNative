import React, { useEffect, useState } from "react";
import { fetchWIPBatches, startWIPBatch, finishWIPBatch } from "../api/wipApi";
import type { WIPBatchDTO } from "../model/WipData";
import QueuedBatchList from "../components/QueuedBatchList";
import InProgressDashboard from "../components/InProgressDashboard";
import BatchExecutionDetail from "../components/BatchExecutionDetail";
import type { AuthUser } from "../../auth/model/AuthUser";
import "../styles/wip.css";

type Props = {
  user: AuthUser;
};

const WorkInProgressPage: React.FC<Props> = ({ user }) => {
  const [batches, setBatches] = useState<WIPBatchDTO[]>([]);
  const [selectedBatch, setSelectedBatch] = useState<WIPBatchDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadBatches();
  }, []);

  const loadBatches = async () => {
    try {
      setLoading(true);
      const data = await fetchWIPBatches();
      setBatches(data);
      if (selectedBatch) {
        const updated = data.find(b => b.id === selectedBatch.id);
        if (updated) setSelectedBatch(updated);
      }
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleStart = async (id: number) => {
    try {
      setLoading(true);
      await startWIPBatch(id);
      await refreshData();
    } catch (err: any) {
      alert(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleFinish = async (id: number) => {
    try {
      setLoading(true);
      await finishWIPBatch(id);
      await refreshData();
    } catch (err: any) {
      alert(err.message);
    } finally {
      setLoading(false);
    }
  };

  const refreshData = async () => {
    const data = await fetchWIPBatches();
    setBatches(data);
    if (selectedBatch) {
      const updated = data.find(b => b.id === selectedBatch.id);
      setSelectedBatch(updated || null);
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
          onStart={handleStart}
          onFinish={handleFinish}
        />
      </div>

      {error && <div style={{ position: "absolute", bottom: 20, right: 20, background: "#fef2f2", color: "#b91c1c", padding: "12px", borderRadius: "8px", border: "1px solid #fee2e2" }}>{error}</div>}
    </div>
  );
};

export default WorkInProgressPage;
