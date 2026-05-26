import React, { useEffect, useState } from 'react';
import { getRequestById, type RequestDetailDTO } from '../api/requestApi';
import { useSse } from '../../utils/useSse';
import '../styles/request.css';

interface RequestDetailProps {
  id: number;
  onBack?: () => void;
  inline?: boolean;
}

function getSampleStatusClass(status?: string | null) {
  const normalized = status?.trim().toUpperCase();

  if (normalized === 'FAILED' || normalized === 'FAIL') {
    return 'failed';
  }

  if (normalized === 'PARTIAL_FAILED') {
    return 'partial-failed';
  }

  return '';
}

export const RequestDetail: React.FC<RequestDetailProps> = ({ id, onBack, inline = false }) => {
  const [request, setRequest] = useState<RequestDetailDTO | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchDetail = async () => {
    try {
      const data = await getRequestById(id);
      setRequest(data);
    } catch (error) {
      console.error('Failed to load request detail', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDetail();
  }, [id]);

  useSse('REQUEST_UPDATED', fetchDetail);

  if (loading) return <p>Loading...</p>;
  if (!request) return <p>No request found</p>;

  return (
    <div className={`request-detail ${inline ? 'request-detail-inline' : 'card'}`}>
      <div className="flex" style={{ justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', gap: '12px' }}>
        <h2 style={{ margin: 0 }}>Request Detail - #{request.id}</h2>
        {!inline && onBack && (
          <button className="button secondary" onClick={onBack}>
            Back
          </button>
        )}
      </div>

      <div className="column">
        <div className="form-group">
          <label className="label">Title</label>
          <div style={{ fontSize: '18px', fontWeight: 'bold' }}>{request.title}</div>
        </div>

        <div className="flex mb-2">
          <div className="form-group" style={{ flex: 1 }}>
            <label className="label">Status</label>
            <div>
              <span className={`tag ${request.status.toLowerCase()}`}>
                {request.status}
              </span>
            </div>
          </div>
          <div className="form-group" style={{ flex: 1 }}>
            <label className="label">Priority</label>
            <div>
              <span className="tag">
                {request.priority}
              </span>
            </div>
          </div>
        </div>

        {request.status === 'REJECTED' && request.rejectReason && (
          <div className="form-group" style={{ marginBottom: '16px' }}>
            <label className="label" style={{ color: '#dc2626' }}>Reject reason</label>
            <div style={{
              padding: '12px',
              background: '#fff5f5',
              border: '1px solid #fecaca',
              borderRadius: '8px',
              color: '#991b1b',
              fontWeight: 500,
              fontSize: '14px'
            }}>
              {request.rejectReason}
            </div>
          </div>
        )}

        <div className="form-group">
          <label className="label">Description</label>
          <div className="card" style={{ background: '#fcfcfc', minHeight: '60px', marginBottom: '16px' }}>
            {request.description || <span className="text-muted">No description</span>}
          </div>
        </div>

        <div className="form-group">
          <label className="label">Samples and recipes</label>
          <div className="request-sample-list">
            {request.samples && request.samples.length > 0 ? (
              request.samples.map((sample, index: number) => (
                <div key={index} className="request-sample-card">
                  <div className="request-sample-main">
                    <span className="request-sample-barcode">{sample.barcode}</span>
                    <span className="request-sample-recipe">
                      Recipe: {sample.recipeName || 'Unassigned'}
                    </span>
                    <span className={`tag request-sample-status ${getSampleStatusClass(sample.status)}`}>
                      {sample.status || 'UNKNOWN'}
                    </span>
                  </div>
                  <div className="request-sample-meta">
                    <div className="request-sample-meta-item">
                      <span className="label-inline">Recipe</span>
                      <span>{sample.recipeName || 'Unassigned'}</span>
                    </div>
                    <div className="request-sample-meta-item">
                      <span className="label-inline">Recipe Parameters</span>
                      <span className="request-sample-params">
                        {sample.recipeParameters || 'No parameters'}
                      </span>
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className="text-muted">No samples</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
