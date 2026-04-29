import { useState } from "react";
import "../styles/style.css";
type Props = {
  onClose: () => void;
  onSubmit: (reason: string) => Promise<void>;
};

function RejectModal({ onClose, onSubmit }: Props) {
  const [reason, setReason] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (!reason.trim()) {
      alert("Reject reason is required");
      return;
    }

    setLoading(true);
    await onSubmit(reason);
    setLoading(false);
  };

  return (
    <div className="modal-overlay">
      <div className="modal">
        <h3>Reject Order</h3>

        <textarea
          placeholder="Enter reject reason..."
          value={reason}
          onChange={(e) => setReason(e.target.value)}
        />

        <div className="modal-actions">
          <button
            className="button secondary"
            onClick={onClose}
            disabled={loading}
          >
            Cancel
          </button>

          <button
            className="button danger"
            onClick={handleSubmit}
            disabled={loading}
          >
            Submit
          </button>
        </div>
      </div>
    </div>
  );
}

export default RejectModal;