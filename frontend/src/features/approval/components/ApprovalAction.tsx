import React, { useState } from "react";
import "../styles/style.css";
import "@/styles/global.css";
import RejectModal from "./RejectModal";

type Order = {
  id: number;
};

type Props = {
  order: Order | null;
  onApprove: (id: number) => Promise<void>;
  onReject: (id: number, reason: string) => Promise<void>;
};

function ApprovalAction({ order, onApprove, onReject }: Props) {
  const [showModal, setShowModal] = useState(false);
  const [loading, setLoading] = useState(false);

  if (!order) {
    return <div className="card">No order selected</div>;
  }

  const handleApprove = async () => {
    setLoading(true);
    await onApprove(order.id);
    setLoading(false);
  };

  const handleRejectSubmit = async (reason: string) => {
    await onReject(order.id, reason);
    setShowModal(false);
  };

  return (
    <div className="card column">
      <h3>Action</h3>

      {/* Approve */}
      <button
        className="button primary"
        onClick={handleApprove}
        disabled={loading}
      >
        Approve
      </button>

      {/* Reject */}
      <button
        className="button danger mt-2"
        onClick={() => setShowModal(true)}
      >
        Reject
      </button>

      {showModal && (
        <RejectModal
          onClose={() => setShowModal(false)}
          onSubmit={handleRejectSubmit}
        />
      )}
    </div>
  );
}

export default ApprovalAction;